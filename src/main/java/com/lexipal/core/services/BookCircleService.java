package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.BookCircleCreateRequestDTO;
import com.lexipal.core.dtos.requests.BookCreateRequestDTO;
import com.lexipal.core.dtos.responses.BookCircleCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookCircleReadResponseDTO;
import com.lexipal.core.dtos.responses.BookCreateResponseDTO;
import com.lexipal.core.dtos.responses.CircleMemberDetailResponseDTO;
import com.lexipal.core.dtos.responses.CircleMemberProgressDTO;
import com.lexipal.core.dtos.responses.CircleNoteDTO;
import com.lexipal.core.dtos.responses.CircleSessionDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.entities.BookCircle;
import com.lexipal.core.entities.CircleMember;
import com.lexipal.core.entities.CircleMemberBlock;
import com.lexipal.core.entities.CircleMemberRole;
import com.lexipal.core.entities.CircleStatus;
import com.lexipal.core.entities.CircleVisibility;
import com.lexipal.core.entities.Note;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.BookCircleRepository;
import com.lexipal.core.repositories.BookRepository;
import com.lexipal.core.repositories.CircleMemberBlockRepository;
import com.lexipal.core.repositories.CircleMemberRepository;
import com.lexipal.core.repositories.NoteRepository;
import com.lexipal.core.repositories.SessionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookCircleService implements BookCircleServiceInterface
{
    private static final Logger logger = LogManager.getLogger(BookCircleService.class);

    private final BookCircleRepository bookCircleRepository;
    private final CircleMemberBlockRepository circleMemberBlockRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final NoteRepository noteRepository;
    private final UserServiceInterface userService;
    private final BookServiceInterface bookService;
    private final InviteCodeGenerator inviteCodeGenerator;

    @Autowired
    public BookCircleService(final BookCircleRepository bookCircleRepository,
                             final CircleMemberBlockRepository circleMemberBlockRepository,
                             final CircleMemberRepository circleMemberRepository,
                             final BookRepository bookRepository,
                             final SessionRepository sessionRepository,
                             final NoteRepository noteRepository,
                             final UserServiceInterface userService,
                             final BookServiceInterface bookService,
                             final InviteCodeGenerator inviteCodeGenerator)
    {
        this.bookCircleRepository = bookCircleRepository;
        this.circleMemberBlockRepository = circleMemberBlockRepository;
        this.circleMemberRepository = circleMemberRepository;
        this.bookRepository = bookRepository;
        this.sessionRepository = sessionRepository;
        this.noteRepository = noteRepository;
        this.userService = userService;
        this.bookService = bookService;
        this.inviteCodeGenerator = inviteCodeGenerator;
    }

    @Override
    @Transactional
    public BookCircleCreateResponseDTO createCircle(BookCircleCreateRequestDTO request, String keycloakSub)
    {
        User owner = userService.getOrCreateUser(keycloakSub);

        Book ownerBook = resolveOwnerBook(request, owner, keycloakSub);

        BookCircle circle = new BookCircle();
        circle.setInviteCode(inviteCodeGenerator.generateUnique(bookCircleRepository));
        circle.setBookTitle(ownerBook.getTitle());
        circle.setBookAuthor(ownerBook.getAuthor());
        circle.setTotalPages(ownerBook.getTotalPages());
        circle.setMemberCap(request.getMemberCap());
        circle.setVisibility(request.getVisibility());
        circle.setStatus(CircleStatus.ACTIVE);
        circle.setOwner(owner);

        BookCircle savedCircle = bookCircleRepository.save(circle);

        CircleMember ownerMember = new CircleMember();
        ownerMember.setCircle(savedCircle);
        ownerMember.setUser(owner);
        ownerMember.setBook(ownerBook);
        ownerMember.setRole(CircleMemberRole.OWNER);
        circleMemberRepository.save(ownerMember);

        logger.info("Created circle {} with invite code {} for owner {}",
                savedCircle.getId(), savedCircle.getInviteCode(), owner.getId());

        return new BookCircleCreateResponseDTO(
                savedCircle.getId(),
                savedCircle.getInviteCode(),
                savedCircle.getBookTitle(),
                savedCircle.getBookAuthor(),
                savedCircle.getTotalPages(),
                savedCircle.getMemberCap(),
                savedCircle.getVisibility(),
                savedCircle.getStatus(),
                savedCircle.getCreatedAt()
        );
    }

    private Book resolveOwnerBook(BookCircleCreateRequestDTO request, User owner, String keycloakSub)
    {
        if (request.getExistingBookId() != null)
        {
            if (request.getBookTitle() != null || request.getBookAuthor() != null || request.getTotalPages() != null)
            {
                throw new IllegalArgumentException(
                        "Provide either existingBookId or (bookTitle + bookAuthor + totalPages), not both");
            }

            Book existing = bookRepository.findById(request.getExistingBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getExistingBookId()));

            if (!existing.getUser().getKeycloakSub().equals(keycloakSub))
            {
                throw new UnauthorizedActionException("You do not own this book");
            }

            return existing;
        }

        if (request.getBookTitle() == null || request.getBookTitle().isBlank()
                || request.getBookAuthor() == null || request.getBookAuthor().isBlank()
                || request.getTotalPages() == null)
        {
            throw new IllegalArgumentException(
                    "Either existingBookId or all of (bookTitle, bookAuthor, totalPages) must be provided");
        }

        BookCreateResponseDTO created = bookService.addBook(
                new BookCreateRequestDTO(request.getBookTitle(), request.getBookAuthor(), request.getTotalPages()),
                keycloakSub
        );

        return bookRepository.findById(created.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", created.getId()));
    }

    @Override
    @Transactional
    public BookCircleReadResponseDTO joinCircle(String inviteCode, String keycloakSub)
    {
        User user = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findByInviteCodeAndStatus(inviteCode, CircleStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "inviteCode", inviteCode));

        if (circleMemberBlockRepository.existsByCircle_IdAndUser_Id(circle.getId(), user.getId()))
        {
            throw new UnauthorizedActionException(
                    "You have been removed from this circle and cannot rejoin.");
        }

        if (circleMemberRepository.findByCircle_IdAndUser_Id(circle.getId(), user.getId()).isPresent())
        {
            throw new IllegalArgumentException("You are already a member of this circle");
        }

        long currentCount = circleMemberRepository.countByCircle_Id(circle.getId());
        if (currentCount >= circle.getMemberCap())
        {
            throw new IllegalArgumentException("This circle is full");
        }

        BookCreateResponseDTO created = bookService.addBook(
                new BookCreateRequestDTO(circle.getBookTitle(), circle.getBookAuthor(), circle.getTotalPages()),
                keycloakSub
        );

        Book joinedBook = bookRepository.findById(created.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", created.getId()));

        CircleMember member = new CircleMember();
        member.setCircle(circle);
        member.setUser(user);
        member.setBook(joinedBook);
        member.setRole(CircleMemberRole.MEMBER);
        circleMemberRepository.save(member);

        logger.info("User {} joined circle {} via code {}", user.getId(), circle.getId(), inviteCode);

        return buildCircleReadResponse(circle, user);
    }

    @Override
    public BookCircleReadResponseDTO getCircle(Long circleId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "id", circleId));

        circleMemberRepository.findByCircle_IdAndUser_Id(circle.getId(), caller.getId())
                .orElseThrow(() -> new UnauthorizedActionException("You are not a member of this circle"));

        return buildCircleReadResponse(circle, caller);
    }

    @Override
    public List<BookCircleReadResponseDTO> getMyCircles(String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        List<BookCircle> circles = bookCircleRepository.findActiveCirclesForUser(keycloakSub);
        return circles.stream()
                .map(c -> buildCircleReadResponse(c, caller))
                .collect(Collectors.toList());
    }

    @Override
    public CircleMemberDetailResponseDTO getMemberDetails(Long circleId, Long targetUserId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "id", circleId));

        CircleMember target = circleMemberRepository.findByCircle_IdAndUser_Id(circleId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("CircleMember", "userId", targetUserId));

        boolean callerIsOwner = circle.getOwner().getId().equals(caller.getId());
        boolean callerIsTarget = caller.getId().equals(targetUserId);
        boolean callerIsMember = circleMemberRepository
                .findByCircle_IdAndUser_Id(circleId, caller.getId()).isPresent();

        if (!callerIsOwner && !callerIsTarget)
        {
            if (!callerIsMember || circle.getVisibility() != CircleVisibility.ALL_MEMBERS)
            {
                throw new UnauthorizedActionException(
                        "You are not allowed to view this member's details");
            }
        }

        Book book = target.getBook();
        List<Session> sessions = sessionRepository.findByBook_IdOrderBySessionDateDesc(book.getId());
        List<Note> notes = noteRepository.findByBook_IdOrderByCreatedAtDesc(book.getId());

        List<CircleSessionDTO> sessionDtos = sessions.stream()
                .map(this::toCircleSessionDTO)
                .collect(Collectors.toList());

        List<CircleNoteDTO> noteDtos = notes.stream()
                .map(this::toCircleNoteDTO)
                .collect(Collectors.toList());

        Double percent = computeProgressPercentage(book.getReadPages(), book.getTotalPages());

        return new CircleMemberDetailResponseDTO(
                circle.getId(),
                target.getUser().getId(),
                target.getUser().getNickname(),
                target.getRole(),
                target.getJoinedAt(),
                book.getReadPages(),
                book.getTotalPages(),
                percent,
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getStatus(),
                sessionDtos,
                noteDtos
        );
    }

    @Override
    @Transactional
    public void removeMember(Long circleId, Long targetUserId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "id", circleId));

        if (!circle.getOwner().getId().equals(caller.getId()))
        {
            throw new UnauthorizedActionException("Only the circle owner can remove members");
        }

        CircleMember membership = circleMemberRepository.findByCircle_IdAndUser_Id(circleId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircleMember", "circleId+userId", circleId + "/" + targetUserId));

        if (membership.getRole() == CircleMemberRole.OWNER)
        {
            throw new IllegalArgumentException("Owner cannot be removed from the circle.");
        }

        if (!circleMemberBlockRepository.existsByCircle_IdAndUser_Id(circleId, targetUserId))
        {
            CircleMemberBlock block = new CircleMemberBlock();
            block.setCircle(circle);
            block.setUser(membership.getUser());
            block.setBlockedBy(caller);
            circleMemberBlockRepository.save(block);
        }

        circleMemberRepository.delete(membership);
        logger.info("Owner {} removed user {} from circle {}", caller.getId(), targetUserId, circleId);
    }

    @Override
    @Transactional
    public void leaveCircle(Long circleId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        CircleMember membership = circleMemberRepository.findByCircle_IdAndUser_Id(circleId, caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircleMember", "circleId+userId", circleId + "/" + caller.getId()));

        if (membership.getRole() == CircleMemberRole.OWNER)
        {
            throw new IllegalArgumentException(
                    "Owner cannot leave the circle. Close the circle instead.");
        }

        circleMemberRepository.delete(membership);
        logger.info("User {} left circle {}", caller.getId(), circleId);
    }

    @Override
    @Transactional
    public void closeCircle(Long circleId, String keycloakSub)
    {
        User caller = userService.getOrCreateUser(keycloakSub);

        BookCircle circle = bookCircleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCircle", "id", circleId));

        if (!circle.getOwner().getId().equals(caller.getId()))
        {
            throw new UnauthorizedActionException("Only the circle owner can close this circle");
        }

        circle.setStatus(CircleStatus.CLOSED);
        bookCircleRepository.save(circle);
        logger.info("Circle {} closed by owner {}", circleId, caller.getId());
    }

    private BookCircleReadResponseDTO buildCircleReadResponse(BookCircle circle, User caller)
    {
        boolean callerIsOwner = circle.getOwner().getId().equals(caller.getId());

        List<CircleMember> members = circleMemberRepository.findByCircle_Id(circle.getId());

        boolean revealAllProgress =
                circle.getVisibility() == CircleVisibility.ALL_MEMBERS || callerIsOwner;

        List<CircleMemberProgressDTO> memberDtos = members.stream()
                .map(m -> toMemberProgressDTO(m, caller, revealAllProgress))
                .collect(Collectors.toList());

        return new BookCircleReadResponseDTO(
                circle.getId(),
                circle.getInviteCode(),
                circle.getBookTitle(),
                circle.getBookAuthor(),
                circle.getTotalPages(),
                circle.getMemberCap(),
                members.size(),
                circle.getVisibility(),
                circle.getStatus(),
                circle.getOwner().getNickname(),
                callerIsOwner,
                circle.getCreatedAt(),
                memberDtos
        );
    }

    private CircleMemberProgressDTO toMemberProgressDTO(CircleMember member, User caller, boolean revealAllProgress)
    {
        Book book = member.getBook();
        boolean isCallerRow = member.getUser().getId().equals(caller.getId());
        boolean revealThisRow = revealAllProgress || isCallerRow;

        Integer readPages = revealThisRow ? book.getReadPages() : null;
        Integer totalPages = revealThisRow ? book.getTotalPages() : null;
        Double percent = revealThisRow ? computeProgressPercentage(book.getReadPages(), book.getTotalPages()) : null;

        return new CircleMemberProgressDTO(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getRole(),
                readPages,
                totalPages,
                percent,
                member.getJoinedAt()
        );
    }

    private Double computeProgressPercentage(Integer readPages, Integer totalPages)
    {
        if (readPages == null || totalPages == null || totalPages == 0)
        {
            return 0.0;
        }
        return ((double) readPages / totalPages) * 100.0;
    }

    private CircleSessionDTO toCircleSessionDTO(Session s)
    {
        return new CircleSessionDTO(
                s.getId(),
                s.getSessionDate(),
                s.getCompletedAt(),
                s.getDurationSeconds(),
                s.getStartPage(),
                s.getEndPage(),
                s.getPagesRead(),
                s.getStatus(),
                s.getReflection(),
                s.getCapturedWordCount()
        );
    }

    private CircleNoteDTO toCircleNoteDTO(Note n)
    {
        return new CircleNoteDTO(
                n.getId(),
                n.getQuotedText(),
                n.getPageNumber(),
                n.getChapterLabel(),
                n.getCreatedAt(),
                n.getSourceSessionId()
        );
    }
}
