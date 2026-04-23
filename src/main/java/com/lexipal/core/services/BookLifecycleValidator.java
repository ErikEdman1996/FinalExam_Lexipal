package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.BookUpdateRequestDTO;
import com.lexipal.core.entities.Book;
import org.springframework.stereotype.Service;

@Service
public class BookLifecycleValidator
{
    public BookUpdateResolution resolveUpdate(Book book, BookUpdateRequestDTO dto)
    {
        if (book.getStatus() == Book.BookStatus.DELETED)
        {
            throw new IllegalArgumentException("Deleted books cannot be updated.");
        }

        String title = dto.getTitle() != null ? dto.getTitle().trim() : book.getTitle();
        String author = dto.getAuthor() != null ? dto.getAuthor().trim() : book.getAuthor();
        int readPages = dto.getReadPages() != null ? dto.getReadPages() : defaultReadPages(book);
        int totalPages = dto.getTotalPages() != null ? dto.getTotalPages() : book.getTotalPages();

        assertReadPagesWithinTotalPages(readPages, totalPages);

        Book.BookStatus resolvedStatus = resolveStatus(book.getStatus(), dto.getStatus(), readPages, totalPages);
        return new BookUpdateResolution(title, author, readPages, totalPages, resolvedStatus);
    }

    public void assertCanBeUsedForCircle(Book book)
    {
        if (book.getStatus() == Book.BookStatus.ARCHIVED || book.getStatus() == Book.BookStatus.DELETED)
        {
            throw new IllegalArgumentException("Archived or deleted books cannot be used to create a circle.");
        }
    }

    public void assertAllowsSessionMutations(Book book)
    {
        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED)
        {
            throw new IllegalArgumentException("Sessions cannot be modified for archived or deleted books.");
        }
    }

    public void assertAllowsNoteMutations(Book book)
    {
        if (book.getStatus() == Book.BookStatus.DELETED || book.getStatus() == Book.BookStatus.ARCHIVED)
        {
            throw new IllegalArgumentException("Notes cannot be modified for archived or deleted books.");
        }
    }

    public void assertReadPagesWithinTotalPages(int readPages, int totalPages)
    {
        if (readPages > totalPages)
        {
            throw new IllegalArgumentException("readPages cannot be greater than totalPages.");
        }
    }

    private Book.BookStatus resolveStatus(Book.BookStatus currentStatus,
                                          Book.BookStatus requestedStatus,
                                          int readPages,
                                          int totalPages)
    {
        if (requestedStatus == null)
        {
            if (currentStatus == Book.BookStatus.ARCHIVED)
            {
                return Book.BookStatus.ARCHIVED;
            }

            return readPages >= totalPages ? Book.BookStatus.COMPLETE : Book.BookStatus.ACTIVE;
        }

        return switch (requestedStatus)
        {
            case ACTIVE -> {
                if (readPages >= totalPages)
                {
                    throw new IllegalArgumentException("An ACTIVE book must have readPages less than totalPages.");
                }
                yield Book.BookStatus.ACTIVE;
            }
            case COMPLETE -> {
                if (readPages < totalPages)
                {
                    throw new IllegalArgumentException("A COMPLETE book must have readPages equal to totalPages.");
                }
                yield Book.BookStatus.COMPLETE;
            }
            case ARCHIVED -> Book.BookStatus.ARCHIVED;
            case DELETED -> throw new IllegalArgumentException("Use DELETE /books/{id} to delete a book.");
        };
    }

    private int defaultReadPages(Book book)
    {
        return book.getReadPages() != null ? book.getReadPages() : 0;
    }

    public record BookUpdateResolution(String title,
                                       String author,
                                       int readPages,
                                       int totalPages,
                                       Book.BookStatus status)
    {
    }
}
