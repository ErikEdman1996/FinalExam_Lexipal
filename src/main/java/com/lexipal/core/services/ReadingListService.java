package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.ReadingListBookSlotRequestDTO;
import com.lexipal.core.dtos.requests.ReadingListCreateRequestDTO;
import com.lexipal.core.dtos.requests.ReadingListUpdateRequestDTO;
import com.lexipal.core.dtos.responses.ReadingListDetailResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSlotResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSubscriptionResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSummaryResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.CatalogBook;
import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.entities.ReadingList;
import com.lexipal.core.entities.ReadingListBook;
import com.lexipal.core.entities.ReadingListStar;
import com.lexipal.core.entities.ReadingListStatus;
import com.lexipal.core.entities.ReadingListSubscription;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CatalogBookRepository;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.ReadingListBookRepository;
import com.lexipal.core.repositories.ReadingListRepository;
import com.lexipal.core.repositories.ReadingListStarRepository;
import com.lexipal.core.repositories.ReadingListSubscriptionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReadingListService implements ReadingListServiceInterface
{
    private static final int LIST_BOOK_COMPLETION_XP = 50;
    private static final int LIST_FULLY_COMPLETED_XP = 200;

    private static final Logger logger = LogManager.getLogger(ReadingListService.class);

    private final ReadingListRepository readingListRepository;
    private final ReadingListBookRepository readingListBookRepository;
    private final ReadingListSubscriptionRepository subscriptionRepository;
    private final ReadingListStarRepository starRepository;
    private final CatalogBookRepository catalogBookRepository;
    private final BookRepository bookRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;

    @Autowired
    public ReadingListService(final ReadingListRepository readingListRepository,
                              final ReadingListBookRepository readingListBookRepository,
                              final ReadingListSubscriptionRepository subscriptionRepository,
                              final ReadingListStarRepository starRepository,
                              final CatalogBookRepository catalogBookRepository,
                              final BookRepository bookRepository,
                              final DailyActivityRepository dailyActivityRepository,
                              final UserServiceInterface userService,
                              final XpService xpService)
    {
        this.readingListRepository = readingListRepository;
        this.readingListBookRepository = readingListBookRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.starRepository = starRepository;
        this.catalogBookRepository = catalogBookRepository;
        this.bookRepository = bookRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.userService = userService;
        this.xpService = xpService;
    }

    @Override
    public ReadingListDetailResponseDTO createList(ReadingListCreateRequestDTO request, String keycloakSub)
    {
        User owner = userService.getOrCreateUser(keycloakSub);

        List<ReadingListBookSlotRequestDTO> slots = request.getBooks();
        validateSlotPositions(slots);

        ReadingList list = new ReadingList();
        list.setTitle(request.getTitle().trim());
        list.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        list.setOwner(owner);
        list.setStatus(request.getStatus() == null ? ReadingListStatus.PUBLISHED : request.getStatus());

        ReadingList savedList = readingListRepository.save(list);

        List<ReadingListBook> slotEntities = new ArrayList<>();
        for (ReadingListBookSlotRequestDTO slot : slots)
        {
            CatalogBook catalogBook = resolveOrCreateCatalogBook(slot.getTitle(), slot.getAuthor());

            ReadingListBook slotEntity = new ReadingListBook();
            slotEntity.setList(savedList);
            slotEntity.setCatalogBook(catalogBook);
            slotEntity.setPosition(slot.getPosition());
            slotEntity.setNote(slot.getNote());

            slotEntities.add(readingListBookRepository.save(slotEntity));
        }
        savedList.setBooks(slotEntities);

        logger.info("Created reading list {} with {} slots for user {}",
                savedList.getId(), slotEntities.size(), owner.getId());

        return buildDetailResponse(savedList, owner);
    }

    @Override
    public ReadingListDetailResponseDTO updateList(Long listId, ReadingListUpdateRequestDTO request, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingList list = loadOwnedList(listId, caller);

        if (request.getTitle() != null)
        {
            if (request.getTitle().isBlank())
            {
                throw new IllegalArgumentException("Title cannot be blank");
            }
            list.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null)
        {
            list.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null)
        {
            list.setStatus(request.getStatus());
        }

        ReadingList saved = readingListRepository.save(list);
        logger.info("Updated reading list {} by owner {}", listId, caller.getId());

        return buildDetailResponse(saved, caller);
    }

    @Override
    public void deleteList(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingList list = loadOwnedList(listId, caller);

        readingListRepository.delete(list);
        logger.info("Deleted reading list {} by owner {}", listId, caller.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingListDetailResponseDTO getList(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingList list = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingList", "id", listId));

        return buildDetailResponse(list, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingListSummaryResponseDTO> browseLists(String query, Integer minStars, Integer page, Integer size,
                                                           String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : Math.min(size, 100);
        int safeMinStars = minStars == null || minStars < 0 ? 0 : minStars;
        String trimmedQuery = (query == null || query.isBlank()) ? null : query.trim();

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "starCount").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        if (trimmedQuery == null)
        {
            return readingListRepository.browse(ReadingListStatus.PUBLISHED, safeMinStars, pageable)
                    .getContent().stream()
                    .map(l -> buildSummaryResponse(l, caller))
                    .collect(Collectors.toList());
        }

        return readingListRepository.browseWithQuery(ReadingListStatus.PUBLISHED, safeMinStars, trimmedQuery, pageable)
                .getContent().stream()
                .map(l -> buildSummaryResponse(l, caller))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingListSummaryResponseDTO> getMyLists(String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        return readingListRepository.findByOwner_KeycloakSubOrderByCreatedAtDesc(keycloakSub).stream()
                .map(l -> buildSummaryResponse(l, caller))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingListSubscriptionResponseDTO> getMySubscriptions(String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        List<ReadingListSubscription> subs = subscriptionRepository
                .findByUser_KeycloakSubOrderBySubscribedAtDesc(keycloakSub);

        return subs.stream()
                .map(sub -> buildSubscriptionResponse(sub, caller))
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingList list = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingList", "id", listId));

        if (list.getStatus() != ReadingListStatus.PUBLISHED)
        {
            throw new IllegalArgumentException("Only published lists can be subscribed to");
        }

        if (subscriptionRepository.existsByList_IdAndUser_Id(listId, caller.getId()))
        {
            return;
        }

        ReadingListSubscription sub = new ReadingListSubscription();
        sub.setList(list);
        sub.setUser(caller);
        subscriptionRepository.save(sub);

        logger.info("User {} subscribed to reading list {}", caller.getId(), listId);
    }

    @Override
    public void unsubscribe(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingListSubscription sub = subscriptionRepository.findByList_IdAndUser_Id(listId, caller.getId())
                .orElse(null);
        if (sub == null)
        {
            return;
        }
        subscriptionRepository.delete(sub);
        logger.info("User {} unsubscribed from reading list {}", caller.getId(), listId);
    }

    @Override
    public void starList(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        ReadingList list = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingList", "id", listId));

        if (starRepository.existsByList_IdAndUser_Id(listId, caller.getId()))
        {
            return;
        }

        ReadingListStar star = new ReadingListStar();
        star.setList(list);
        star.setUser(caller);
        starRepository.save(star);

        readingListRepository.incrementStarCount(listId);
        logger.info("User {} starred reading list {}", caller.getId(), listId);
    }

    @Override
    public void unstarList(Long listId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);
        int removed = starRepository.deleteByListIdAndUserId(listId, caller.getId());
        if (removed > 0)
        {
            readingListRepository.decrementStarCount(listId);
            logger.info("User {} unstarred reading list {}", caller.getId(), listId);
        }
    }

    @Override
    public void onBookCompleted(Book book)
    {
        if (book == null || book.getUser() == null)
        {
            return;
        }

        String titleNorm = CatalogBook.normalize(book.getTitle());
        String authorNorm = CatalogBook.normalize(book.getAuthor());
        if (titleNorm == null || authorNorm == null)
        {
            return;
        }

        List<Long> catalogIds = bookRepository.findCatalogBookIdsByNormalized(titleNorm, authorNorm);
        if (catalogIds.isEmpty())
        {
            return;
        }

        Long userId = book.getUser().getId();

        for (Long catalogId : catalogIds)
        {
            List<ReadingListSubscription> activeSubs =
                    subscriptionRepository.findActiveSubscriptionsByCatalogBookForUser(userId, catalogId);
            if (activeSubs.isEmpty())
            {
                continue;
            }

            boolean alreadyAwardedForBook = dailyActivityRepository
                    .existsByUser_IdAndActivityTypeAndExternalRefId(
                            userId, DailyActivity.ActivityType.READING_LIST_BOOK_COMPLETED, catalogId);

            if (!alreadyAwardedForBook)
            {
                recordActivity(book.getUser(),
                        DailyActivity.ActivityType.READING_LIST_BOOK_COMPLETED,
                        LIST_BOOK_COMPLETION_XP,
                        catalogId);
                xpService.awardXp(book.getUser(), LIST_BOOK_COMPLETION_XP);
                logger.info("Awarded {} XP to user {} for reading-list book {}",
                        LIST_BOOK_COMPLETION_XP, userId, catalogId);
            }

            for (ReadingListSubscription sub : activeSubs)
            {
                maybeCompleteSubscription(sub);
            }
        }
    }

    private void maybeCompleteSubscription(ReadingListSubscription sub)
    {
        if (sub.getCompletedAt() != null)
        {
            return;
        }

        List<ReadingListBook> slots = readingListBookRepository.findByList_IdOrderByPositionAsc(sub.getList().getId());
        if (slots.isEmpty())
        {
            return;
        }

        List<Long> catalogIds = slots.stream()
                .map(s -> s.getCatalogBook().getId())
                .collect(Collectors.toList());

        Set<Long> completed = new HashSet<>(bookRepository.findCompletedCatalogBookIdsForUser(
                sub.getUser().getId(), catalogIds));

        if (!completed.containsAll(catalogIds))
        {
            return;
        }

        sub.setCompletedAt(LocalDateTime.now());
        subscriptionRepository.save(sub);

        boolean alreadyAwarded = dailyActivityRepository.existsByUser_IdAndActivityTypeAndExternalRefId(
                sub.getUser().getId(), DailyActivity.ActivityType.READING_LIST_COMPLETED, sub.getId());

        if (!alreadyAwarded)
        {
            recordActivity(sub.getUser(),
                    DailyActivity.ActivityType.READING_LIST_COMPLETED,
                    LIST_FULLY_COMPLETED_XP,
                    sub.getId());
            xpService.awardXp(sub.getUser(), LIST_FULLY_COMPLETED_XP);
            logger.info("Awarded {} XP to user {} for completing reading list subscription {}",
                    LIST_FULLY_COMPLETED_XP, sub.getUser().getId(), sub.getId());
        }
    }

    private void recordActivity(User user, DailyActivity.ActivityType type, int xp, Long externalRefId)
    {
        DailyActivity activity = new DailyActivity();
        activity.setUser(user);
        activity.setActivityDate(LocalDate.now());
        activity.setActivityType(type);
        activity.setXpEarned(xp);
        activity.setExternalRefId(externalRefId);
        dailyActivityRepository.save(activity);
    }

    private CatalogBook resolveOrCreateCatalogBook(String title, String author)
    {
        String titleNorm = CatalogBook.normalize(title);
        String authorNorm = CatalogBook.normalize(author);

        return catalogBookRepository.findByTitleNormalizedAndAuthorNormalized(titleNorm, authorNorm)
                .orElseGet(() ->
                {
                    CatalogBook cb = new CatalogBook();
                    cb.setTitle(title.trim());
                    cb.setAuthor(author.trim());
                    return catalogBookRepository.save(cb);
                });
    }

    private void validateSlotPositions(List<ReadingListBookSlotRequestDTO> slots)
    {
        Set<Integer> seenPositions = new HashSet<>();
        Set<String> seenKeys = new HashSet<>();
        for (ReadingListBookSlotRequestDTO slot : slots)
        {
            if (!seenPositions.add(slot.getPosition()))
            {
                throw new IllegalArgumentException("Duplicate position: " + slot.getPosition());
            }
            String key = CatalogBook.normalize(slot.getTitle()) + "::" + CatalogBook.normalize(slot.getAuthor());
            if (!seenKeys.add(key))
            {
                throw new IllegalArgumentException(
                        "Duplicate book in list: " + slot.getTitle() + " by " + slot.getAuthor());
            }
        }
    }

    private ReadingList loadOwnedList(Long listId, User caller)
    {
        ReadingList list = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingList", "id", listId));
        if (!list.getOwner().getId().equals(caller.getId()))
        {
            throw new UnauthorizedActionException("Only the list owner can perform this action");
        }
        return list;
    }

    private ReadingListDetailResponseDTO buildDetailResponse(ReadingList list, User caller)
    {
        List<ReadingListBook> slots = readingListBookRepository.findByList_IdOrderByPositionAsc(list.getId());
        boolean callerIsOwner = list.getOwner().getId().equals(caller.getId());
        boolean hasStarred = starRepository.existsByList_IdAndUser_Id(list.getId(), caller.getId());
        boolean isSubscribed = subscriptionRepository.existsByList_IdAndUser_Id(list.getId(), caller.getId());

        Set<Long> ownedCatalogIds;
        Set<Long> completedCatalogIds;
        if (isSubscribed && !slots.isEmpty())
        {
            List<Long> catalogIds = slots.stream()
                    .map(s -> s.getCatalogBook().getId())
                    .collect(Collectors.toList());
            ownedCatalogIds = new HashSet<>(
                    bookRepository.findOwnedCatalogBookIdsForUser(caller.getId(), catalogIds));
            completedCatalogIds = new HashSet<>(
                    bookRepository.findCompletedCatalogBookIdsForUser(caller.getId(), catalogIds));
        }
        else
        {
            ownedCatalogIds = Collections.emptySet();
            completedCatalogIds = Collections.emptySet();
        }

        List<ReadingListSlotResponseDTO> slotDtos = slots.stream()
                .map(s -> toSlotDTO(s, isSubscribed, ownedCatalogIds, completedCatalogIds))
                .collect(Collectors.toList());

        return new ReadingListDetailResponseDTO(
                list.getId(),
                list.getTitle(),
                list.getDescription(),
                list.getOwner().getNickname(),
                list.getOwner().getId(),
                list.getStatus(),
                list.getStarCount(),
                callerIsOwner,
                hasStarred,
                isSubscribed,
                list.getCreatedAt(),
                list.getUpdatedAt(),
                slotDtos
        );
    }

    private ReadingListSummaryResponseDTO buildSummaryResponse(ReadingList list, User caller)
    {
        long bookCount = readingListBookRepository.countByList_Id(list.getId());
        boolean callerIsOwner = list.getOwner().getId().equals(caller.getId());
        boolean hasStarred = starRepository.existsByList_IdAndUser_Id(list.getId(), caller.getId());
        boolean isSubscribed = subscriptionRepository.existsByList_IdAndUser_Id(list.getId(), caller.getId());

        return new ReadingListSummaryResponseDTO(
                list.getId(),
                list.getTitle(),
                list.getDescription(),
                list.getOwner().getNickname(),
                list.getOwner().getId(),
                list.getStatus(),
                list.getStarCount(),
                (int) bookCount,
                callerIsOwner,
                hasStarred,
                isSubscribed,
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }

    private ReadingListSubscriptionResponseDTO buildSubscriptionResponse(ReadingListSubscription sub, User caller)
    {
        List<ReadingListBook> slots = readingListBookRepository.findByList_IdOrderByPositionAsc(sub.getList().getId());

        int completed = 0;
        if (!slots.isEmpty())
        {
            List<Long> catalogIds = slots.stream()
                    .map(s -> s.getCatalogBook().getId())
                    .collect(Collectors.toList());
            completed = bookRepository.findCompletedCatalogBookIdsForUser(caller.getId(), catalogIds).size();
        }

        return new ReadingListSubscriptionResponseDTO(
                sub.getList().getId(),
                sub.getList().getTitle(),
                sub.getList().getOwner().getNickname(),
                sub.getSubscribedAt(),
                sub.getCompletedAt(),
                slots.size(),
                completed
        );
    }

    private ReadingListSlotResponseDTO toSlotDTO(ReadingListBook slot, boolean isSubscribed,
                                                 Set<Long> ownedCatalogIds, Set<Long> completedCatalogIds)
    {
        CatalogBook cb = slot.getCatalogBook();
        Boolean inMyLibrary = isSubscribed ? ownedCatalogIds.contains(cb.getId()) : null;
        Boolean completedByMe = isSubscribed ? completedCatalogIds.contains(cb.getId()) : null;

        return new ReadingListSlotResponseDTO(
                cb.getId(),
                slot.getPosition(),
                cb.getTitle(),
                cb.getAuthor(),
                slot.getNote(),
                inMyLibrary,
                completedByMe
        );
    }
}
