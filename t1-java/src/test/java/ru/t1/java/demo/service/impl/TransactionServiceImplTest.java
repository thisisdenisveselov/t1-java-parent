package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.exception.AccountNotOpenException;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private UUID transactionId;
    private Transaction transaction;
    private Account account;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setBalance(new BigDecimal("100"));
        account.setStatus(AccountStatus.OPEN);

        transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(new BigDecimal("50"));
        transaction.setOperationType(OperationType.INCOMING);
        transaction.setAccount(account);
    }

    @Test
    void getTransactions_shouldReturnAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        var result = transactionService.getTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void getTransactionById_shouldReturnTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        var result = transactionService.getTransaction(1L);

        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_shouldThrowExceptionIfNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransaction(1L));

        assertEquals("Transaction not found with id: 1", exception.getMessage());
    }

    @Test
    void getTransactionByTransactionId_shouldReturnTransaction() {
        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(transaction));

        var result = transactionService.getTransaction(transactionId);

        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        verify(transactionRepository, times(1)).findByTransactionId(transactionId);
    }

    @Test
    void createTransaction_shouldSaveTransaction() {
        when(accountService.getAccount(account.getAccountId())).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        var result = transactionService.createTransaction(transaction);

        assertNotNull(result);
        assertEquals(TransactionStatus.REQUESTED, result.getStatus());
        verify(accountService, times(1)).getAccount(account.getAccountId());
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(TransactionStatus.REQUESTED, transactionCaptor.getValue().getStatus());
    }

    @Test
    void createTransaction_shouldThrowExceptionForClosedAccount() {
        account.setStatus(AccountStatus.CLOSED);
        when(accountService.getAccount(account.getAccountId())).thenReturn(account);

        AccountNotOpenException exception = assertThrows(AccountNotOpenException.class,
                () -> transactionService.createTransaction(transaction));

        assertEquals("Account with ID " + account.getAccountId() + " is not in OPEN status.", exception.getMessage());
    }

    @Test
    void createTransaction_shouldThrowExceptionForInvalidOperationType() {
        transaction.setOperationType(null);
        when(accountService.getAccount(account.getAccountId())).thenReturn(account);

        IllegalOperationTypeException exception = assertThrows(IllegalOperationTypeException.class,
                () -> transactionService.createTransaction(transaction));

        assertEquals("Unknown operation type: null", exception.getMessage());
    }

    @Test
    void updateTransactionById_shouldUpdateTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setStatus(TransactionStatus.ACCEPTED);

        var result = transactionService.updateTransaction(1L, updatedTransaction);

        assertNotNull(result);
        assertEquals(TransactionStatus.ACCEPTED, result.getStatus());
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(TransactionStatus.ACCEPTED, transactionCaptor.getValue().getStatus());
    }

    @Test
    void deleteTransaction_shouldDeleteTransaction() {
        doNothing().when(transactionRepository).deleteById(1L);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void checkRejected_shouldReturnTrueIfRejectedTransactionsExist() {
        when(accountService.getAccount(account.getAccountId())).thenReturn(account);
        when(transactionRepository.existsByAccountAndStatus(account, TransactionStatus.REJECTED)).thenReturn(true);

        boolean result = transactionService.checkRejected(account.getAccountId());

        assertTrue(result);
        verify(transactionRepository, times(1)).existsByAccountAndStatus(account, TransactionStatus.REJECTED);
    }

    @Test
    void checkRejected_shouldReturnFalseIfNoRejectedTransactionsExist() {
        when(accountService.getAccount(account.getAccountId())).thenReturn(account);
        when(transactionRepository.existsByAccountAndStatus(account, TransactionStatus.REJECTED)).thenReturn(false);

        boolean result = transactionService.checkRejected(account.getAccountId());

        assertFalse(result);
        verify(transactionRepository, times(1)).existsByAccountAndStatus(account, TransactionStatus.REJECTED);
    }
}