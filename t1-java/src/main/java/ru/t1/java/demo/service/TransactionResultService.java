package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.kafka.message.TransactionResultMessage;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionResultService {
    private final TransactionService transactionService;
    private final AccountService accountService;

    public void processTransactionResult(TransactionResultMessage transactionResultMessage) {
        TransactionStatus status = transactionResultMessage.getStatus();

        switch (status) {
            case ACCEPTED -> processAcceptedTransaction(transactionResultMessage);
            case REJECTED -> processRejectedTransaction(transactionResultMessage);
            case BLOCKED -> processBlockedTransaction(transactionResultMessage);
            default -> throw new IllegalArgumentException("Unknown transaction status: " + status);
        }
    }

    private void processAcceptedTransaction(TransactionResultMessage transactionResultMessage) {
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.ACCEPTED);
        transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
    }

    private void processRejectedTransaction(TransactionResultMessage transactionResultMessage) {
        adjustAccountBalanceOnRejection(transactionResultMessage);

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.REJECTED);
        transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
    }

    private void processBlockedTransaction(TransactionResultMessage transactionResultMessage) {
        Account account = Account.builder().status(AccountStatus.BLOCKED).build();
        accountService.updateAccount(transactionResultMessage.getAccountId(), account);

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.BLOCKED);
        transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
    }

    private void adjustAccountBalanceOnRejection(TransactionResultMessage transactionResultMessage) {
        OperationType operationType = transactionResultMessage.getOperationType();
        UUID accountId = transactionResultMessage.getAccountId();
        BigDecimal amount = transactionResultMessage.getTransactionAmount();

        if (operationType == OperationType.INCOMING) {
            accountService.decreaseBalance(accountId, amount);
        } else if (operationType == OperationType.OUTGOING) {
            accountService.increaseBalance(accountId, amount);
        } else {
            throw new IllegalOperationTypeException("Unknown operation type: " + operationType);
        }
    }
}
