package ru.t1.java.demo.service;

import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.exception.AccountNotOpenException;
import ru.t1.java.demo.model.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    List<Transaction> parseJson() throws IOException;
    List<Transaction> getTransactions();
    Transaction getTransaction(Long id);
    Transaction getTransaction(UUID transactionId);
    Transaction createTransaction(Transaction transaction);
    Transaction updateTransaction(Long id, Transaction transaction);
    Transaction updateTransaction(UUID transactionId, Transaction transaction);
    void deleteTransaction(Long id);
}
