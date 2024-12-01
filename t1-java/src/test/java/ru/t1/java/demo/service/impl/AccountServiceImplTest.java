package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setAccountId(accountId);
        account.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void testIncreaseBalance_Success() {
        BigDecimal amount = BigDecimal.valueOf(50);
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        accountService.increaseBalance(accountId, amount);

        assertEquals(BigDecimal.valueOf(150), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testIncreaseBalance_InvalidAmount_Null() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.increaseBalance(accountId, null));

        assertEquals("Invalid amount: null", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testIncreaseBalance_InvalidAmount_Zero() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.increaseBalance(accountId, BigDecimal.ZERO));

        assertEquals("Invalid amount: 0", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testDecreaseBalance_Success() {
        BigDecimal amount = BigDecimal.valueOf(30);
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        accountService.decreaseBalance(accountId, amount);

        assertEquals(BigDecimal.valueOf(70), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testDecreaseBalance_InvalidAmount_Null() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.decreaseBalance(accountId, null));

        assertEquals("Invalid amount: null", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testDecreaseBalance_InvalidAmount_Zero() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.decreaseBalance(accountId, BigDecimal.ZERO));

        assertEquals("Invalid amount: 0", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testIncreaseBalance_AccountNotFound() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accountService.increaseBalance(accountId, BigDecimal.valueOf(50)));

        assertEquals("Account not found with accountId: " + accountId, exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testDecreaseBalance_AccountNotFound() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accountService.decreaseBalance(accountId, BigDecimal.valueOf(50)));

        assertEquals("Account not found with accountId: " + accountId, exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }
}
