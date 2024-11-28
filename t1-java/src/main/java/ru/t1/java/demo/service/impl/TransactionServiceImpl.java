package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.exception.AccountNotOpenException;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@DependsOn("AccountServiceImpl")
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    /*@PostConstruct
    void init() {
        List<Transaction> transactions = new ArrayList<>();
        try {
            transactions = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
        if (!transactions.isEmpty()) {
            transactions.forEach(transaction ->
                    transaction.setAccount(accountService.getAccount(transaction.getAccount().getId())));
            transactionRepository.saveAll(transactions);
        }
    }*/

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Transaction> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        TransactionDto[] transactions = mapper
                .readValue(new File("t1-java/src/main/resources/MOCK_DATA_TRANSACTIONS.json"), TransactionDto[].class);

        return Arrays.stream(transactions)
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Metric(1)
    public List<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @LogDataSourceError
    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException(String.format("%s with id = %d not found", "Transaction", id)));
    }

    @LogDataSourceError
    @Override
    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findByTransactionId(transactionId).
                orElseThrow(() -> new EntityNotFoundException("Transaction not found with transactionId: " + transactionId));
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Account account = accountService.getAccount(transaction.getAccount().getAccountId());
        if(account.getStatus() != AccountStatus.OPEN)
            throw new AccountNotOpenException("Account with ID " + account.getAccountId() + " is not in OPEN status.");

        if(transaction.getOperationType() == OperationType.INCOMING)
            accountService.increaseBalance(account.getAccountId(), transaction.getAmount());
        else if(transaction.getOperationType() == OperationType.OUTGOING)
            accountService.decreaseBalance(account.getAccountId(), transaction.getAmount());
        else
            throw new IllegalOperationTypeException("Unknown operation type: " + transaction.getOperationType());

        transaction.setStatus(TransactionStatus.REQUESTED);
        transaction.setTimestamp(Instant.now());
        transaction.setAccount(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @LogDataSourceError
    @Metric(1)
    @Transactional
    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction updatedTransaction = getTransaction(id);

        if (transaction.getStatus() != null)
            updatedTransaction.setStatus(transaction.getStatus());

        return transactionRepository.save(updatedTransaction);
    }

    @Override
    @LogDataSourceError
    @Transactional
    public Transaction updateTransaction(UUID transactionId, Transaction transaction) {
        Transaction updatedTransaction = getTransaction(transactionId);

        if (transaction.getStatus() != null)
            updatedTransaction.setStatus(transaction.getStatus());

        return transactionRepository.save(updatedTransaction);
    }

    @Override
    @LogDataSourceError
    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public boolean checkRejected(UUID accountId) {
        Account account = accountService.getAccount(accountId);
        return transactionRepository.existsByAccountAndStatus(account, TransactionStatus.REJECTED);
    }
}
