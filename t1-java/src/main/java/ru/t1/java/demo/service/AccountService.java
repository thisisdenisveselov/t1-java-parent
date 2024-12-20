package ru.t1.java.demo.service;

import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<Account> parseJson() throws IOException;
    List<Account> getAccounts();
    Account getAccount(Long id);
    Account getAccount(UUID accountId);
    Account createAccount(Account account);
    Account updateAccount(Long id, Account account);
    Account updateAccount(UUID accountId, Account account);
    void deleteAccount(Long id);
    void changeAccountStatus(UUID accountId, AccountStatus newStatus);
    void alterAccountBalance(Transaction transaction, Account account);
    void increaseBalance(UUID accountId, BigDecimal amount);
    void decreaseBalance(UUID accountId, BigDecimal amount);
}
