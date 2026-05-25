package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.WalletResponseDTO;
import com.lexipal.core.entities.CurrencyTransaction;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.CurrencyTransactionType;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceConflictException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.CurrencyTransactionRepository;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CurrencyService
{
    public static final int STREAK_FREEZE_PRICE = 100;
    public static final int SESSION_COMPLETED_COINS = 25;
    public static final int CHALLENGE_PASSED_COINS = 10;
    public static final int CROSSWORD_PASSED_COINS = 10;
    public static final int READING_LIST_BOOK_COMPLETED_COINS = 50;
    public static final int READING_LIST_COMPLETED_COINS = 200;
    public static final int DAILY_QUEST_COMPLETED_COINS = 10;

    private final UserRepository userRepository;
    private final CurrencyTransactionRepository currencyTransactionRepository;
    private final UserServiceInterface userService;
    private final StreakMetaDataServiceInterface streakMetaDataService;

    public CurrencyService(final UserRepository userRepository,
                           final CurrencyTransactionRepository currencyTransactionRepository,
                           final UserServiceInterface userService,
                           final StreakMetaDataServiceInterface streakMetaDataService)
    {
        this.userRepository = userRepository;
        this.currencyTransactionRepository = currencyTransactionRepository;
        this.userService = userService;
        this.streakMetaDataService = streakMetaDataService;
    }

    public WalletResponseDTO getMyWallet(String keycloakSub)
    {
        return toWalletResponse(userService.getOrCreateUser(keycloakSub));
    }

    public WalletResponseDTO getWallet(User user)
    {
        return toWalletResponse(user);
    }

    public void awardCoins(User user, CurrencyTransactionReason reason, String sourceType, Long sourceReferenceId)
    {
        int amount = rewardAmount(reason);
        if (amount <= 0)
        {
            return;
        }

        User lockedUser = lockUser(user);
        int newBalance = currentBalance(lockedUser) + amount;
        lockedUser.setCoinBalance(newBalance);
        userRepository.save(lockedUser);
        saveTransaction(lockedUser, CurrencyTransactionType.EARN, reason, amount, newBalance,
                sourceType, sourceReferenceId);
    }

    public void spendCoins(User user, int amount, CurrencyTransactionReason reason,
                           String sourceType, Long sourceReferenceId)
    {
        if (amount <= 0)
        {
            throw new IllegalArgumentException("Coin spend amount must be greater than zero.");
        }

        User lockedUser = lockUser(user);
        int currentBalance = currentBalance(lockedUser);
        if (currentBalance < amount)
        {
            throw new ResourceConflictException("You do not have enough coins.");
        }

        int newBalance = currentBalance - amount;
        lockedUser.setCoinBalance(newBalance);
        userRepository.save(lockedUser);
        saveTransaction(lockedUser, CurrencyTransactionType.SPEND, reason, amount, newBalance,
                sourceType, sourceReferenceId);
    }

    int rewardAmount(CurrencyTransactionReason reason)
    {
        if (reason == null)
        {
            return 0;
        }

        return switch (reason)
        {
            case SESSION_COMPLETED -> SESSION_COMPLETED_COINS;
            case CHALLENGE_PASSED -> CHALLENGE_PASSED_COINS;
            case CROSSWORD_PASSED -> CROSSWORD_PASSED_COINS;
            case READING_LIST_BOOK_COMPLETED -> READING_LIST_BOOK_COMPLETED_COINS;
            case READING_LIST_COMPLETED -> READING_LIST_COMPLETED_COINS;
            case DAILY_QUEST_COMPLETED -> DAILY_QUEST_COMPLETED_COINS;
            case STREAK_FREEZE_PURCHASE, THEME_PURCHASE -> 0;
        };
    }

    private WalletResponseDTO toWalletResponse(User user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User is required.");
        }

        return new WalletResponseDTO(
                currentBalance(user),
                streakMetaDataService.getAvailableStreakFreezes(user)
        );
    }

    private User lockUser(User user)
    {
        if (user == null || user.getId() == null)
        {
            throw new IllegalArgumentException("Persisted user is required for currency updates.");
        }

        return userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));
    }

    private int currentBalance(User user)
    {
        return user.getCoinBalance() == null ? 0 : Math.max(user.getCoinBalance(), 0);
    }

    private void saveTransaction(User user, CurrencyTransactionType type, CurrencyTransactionReason reason,
                                 int amount, int balanceAfter, String sourceType, Long sourceReferenceId)
    {
        CurrencyTransaction transaction = new CurrencyTransaction();
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setReason(reason);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setSourceType(sourceType);
        transaction.setSourceReferenceId(sourceReferenceId);
        currencyTransactionRepository.save(transaction);
    }
}
