package com.lexipal.core.services;

import com.lexipal.core.entities.CurrencyTransaction;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.CurrencyTransactionType;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceConflictException;
import com.lexipal.core.repositories.CurrencyTransactionRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyServiceTest
{
    @Test
    void rewardAmount_usesFixedEligibleRewardTable()
    {
        CurrencyService service = service(mock(UserRepository.class), mock(CurrencyTransactionRepository.class));

        assertEquals(25, service.rewardAmount(CurrencyTransactionReason.SESSION_COMPLETED));
        assertEquals(10, service.rewardAmount(CurrencyTransactionReason.CHALLENGE_PASSED));
        assertEquals(10, service.rewardAmount(CurrencyTransactionReason.CROSSWORD_PASSED));
        assertEquals(50, service.rewardAmount(CurrencyTransactionReason.READING_LIST_BOOK_COMPLETED));
        assertEquals(200, service.rewardAmount(CurrencyTransactionReason.READING_LIST_COMPLETED));
        assertEquals(0, service.rewardAmount(CurrencyTransactionReason.STREAK_FREEZE_PURCHASE));
        assertEquals(0, service.rewardAmount(CurrencyTransactionReason.THEME_PURCHASE));
    }

    @Test
    void awardCoins_updatesBalanceAndLedger()
    {
        UserRepository userRepository = mock(UserRepository.class);
        CurrencyTransactionRepository transactionRepository = mock(CurrencyTransactionRepository.class);
        User user = user(7L, 40);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        service(userRepository, transactionRepository).awardCoins(user,
                CurrencyTransactionReason.SESSION_COMPLETED, "SESSION", 12L);

        ArgumentCaptor<CurrencyTransaction> captor = ArgumentCaptor.forClass(CurrencyTransaction.class);
        verify(transactionRepository).save(captor.capture());
        CurrencyTransaction saved = captor.getValue();
        assertEquals(65, user.getCoinBalance());
        assertEquals(CurrencyTransactionType.EARN, saved.getType());
        assertEquals(CurrencyTransactionReason.SESSION_COMPLETED, saved.getReason());
        assertEquals(25, saved.getAmount());
        assertEquals(65, saved.getBalanceAfter());
        assertEquals("SESSION", saved.getSourceType());
        assertEquals(12L, saved.getSourceReferenceId());
    }

    @Test
    void spendCoins_rejectsInsufficientBalanceWithoutLedgerRow()
    {
        UserRepository userRepository = mock(UserRepository.class);
        CurrencyTransactionRepository transactionRepository = mock(CurrencyTransactionRepository.class);
        User user = user(8L, 99);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(ResourceConflictException.class, () -> service(userRepository, transactionRepository)
                .spendCoins(user, 100, CurrencyTransactionReason.STREAK_FREEZE_PURCHASE,
                        "STREAK_FREEZE", null));

        assertEquals(99, user.getCoinBalance());
    }

    private CurrencyService service(UserRepository userRepository, CurrencyTransactionRepository transactionRepository)
    {
        return new CurrencyService(
                userRepository,
                transactionRepository,
                mock(UserServiceInterface.class),
                mock(StreakMetaDataServiceInterface.class)
        );
    }

    private User user(Long id, int balance)
    {
        User user = new User();
        user.setId(id);
        user.setCoinBalance(balance);
        return user;
    }
}
