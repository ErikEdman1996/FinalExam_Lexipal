package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.ReadingListCreateRequestDTO;
import com.lexipal.core.dtos.requests.ReadingListUpdateRequestDTO;
import com.lexipal.core.dtos.responses.ReadingListDetailResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSubscriptionResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSummaryResponseDTO;
import com.lexipal.core.entities.Book;

import java.util.List;

public interface ReadingListServiceInterface
{
    ReadingListDetailResponseDTO createList(ReadingListCreateRequestDTO request, String keycloakSub);

    ReadingListDetailResponseDTO updateList(Long listId, ReadingListUpdateRequestDTO request, String keycloakSub);

    void deleteList(Long listId, String keycloakSub);

    ReadingListDetailResponseDTO getList(Long listId, String keycloakSub);

    List<ReadingListSummaryResponseDTO> browseLists(String query, Integer minStars, Integer page, Integer size,
                                                    String sort, String keycloakSub);

    List<ReadingListSummaryResponseDTO> getMyLists(String keycloakSub);

    List<ReadingListSubscriptionResponseDTO> getMySubscriptions(String keycloakSub);

    void subscribe(Long listId, String keycloakSub);

    void unsubscribe(Long listId, String keycloakSub);

    void starList(Long listId, String keycloakSub);

    void unstarList(Long listId, String keycloakSub);

    void onBookCompleted(Book book);
}
