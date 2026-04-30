package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.LibraryItemResponseDTO;
import com.lexipal.core.entities.Book;
import com.lexipal.core.models.LibraryItemType;
import com.lexipal.core.models.LibraryProgressUnit;
import com.lexipal.core.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LibraryService
{
    private final BookRepository bookRepository;

    public LibraryService(final BookRepository bookRepository)
    {
        this.bookRepository = bookRepository;
    }

    public List<LibraryItemResponseDTO> getLibraryItems(String keycloakSub)
    {
        List<LibraryItemResponseDTO> items = new ArrayList<>();

        bookRepository.findAllActiveBooks(keycloakSub).stream()
                .map(this::toBookItem)
                .forEach(items::add);

        items.sort(byAddedAtDescending());
        return items;
    }

    private LibraryItemResponseDTO toBookItem(Book book)
    {
        int completedUnits = book.getReadPages() == null ? 0 : book.getReadPages();
        int totalUnits = book.getTotalPages() == null ? 0 : book.getTotalPages();

        return new LibraryItemResponseDTO(
                LibraryItemType.BOOK,
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                completedUnits,
                totalUnits,
                calculateProgress(completedUnits, totalUnits),
                LibraryProgressUnit.PAGE,
                book.getStatus().name(),
                book.getCoverImageUrl(),
                book.getCreatedAt()
        );
    }

    private double calculateProgress(int completedUnits, int totalUnits)
    {
        if (totalUnits == 0)
        {
            return 0.0;
        }
        return (double) completedUnits / totalUnits * 100;
    }

    private Comparator<LibraryItemResponseDTO> byAddedAtDescending()
    {
        return (left, right) -> {
            LocalDateTime leftAddedAt = left.getAddedAt();
            LocalDateTime rightAddedAt = right.getAddedAt();

            if (leftAddedAt == null && rightAddedAt == null)
            {
                return 0;
            }
            if (leftAddedAt == null)
            {
                return 1;
            }
            if (rightAddedAt == null)
            {
                return -1;
            }
            return rightAddedAt.compareTo(leftAddedAt);
        };
    }
}
