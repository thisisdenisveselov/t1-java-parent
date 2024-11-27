package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.util.AccountMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("AccountServiceImpl")
@Slf4j
@RequiredArgsConstructor
@DependsOn("ClientServiceImpl")
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final ClientService clientService;

    /*@PostConstruct
    void init() {
        List<Account> accounts = new ArrayList<>();
        try {
            accounts = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
        if (!accounts.isEmpty()) {
            accounts.forEach(account ->
                    account.setOwner(clientService.getClient(account.getOwner().getId())));
            accountRepository.saveAll(accounts);
        }
    }*/

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Account> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        AccountDto[] accounts = mapper
                .readValue(new File("t1-java/src/main/resources/MOCK_DATA_ACCOUNTS.json"), AccountDto[].class);

        return Arrays.stream(accounts)
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @LogDataSourceError
    public Account getAccount(Long id) {
        return accountRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException(String.format("%s with id = %d not found", "Account", id)));
    }

    @Override
    public Account getAccount(UUID accountId) {
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with accountId: " + accountId));
    }

    @Override
    @LogDataSourceError
    @Transactional
    public Account createAccount(Account account) {
        Client owner = clientService.getClient(account.getOwner().getId());
        account.setOwner(owner);
        account.setStatus(AccountStatus.OPEN);
        return accountRepository.save(account);
    }

    @Override
    @LogDataSourceError
    @Transactional
    public Account updateAccount(Long id, Account account) {
        Account updatedAccount = getAccount(id);

        if (account.getStatus() != null)
            updatedAccount.setStatus(account.getStatus());

        if (account.getFrozenAmount() != null)
            updatedAccount.setFrozenAmount(account.getFrozenAmount());

        return accountRepository.save(updatedAccount);
    }

    @Override
    @LogDataSourceError
    @Transactional
    public Account updateAccount(UUID accountId, Account account) {
        Account updatedAccount = getAccount(accountId);

        if (account.getStatus() != null)
            updatedAccount.setStatus(account.getStatus());

        if (account.getFrozenAmount() != null)
            updatedAccount.setFrozenAmount(account.getFrozenAmount());

        return accountRepository.save(updatedAccount);
    }

    @Override
    @LogDataSourceError
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void increaseBalance(UUID accountId, BigDecimal amount) {
        Account account = getAccount(accountId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        BigDecimal newBalance = account.getBalance().add(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void decreaseBalance(UUID accountId, BigDecimal amount) {
        Account account = getAccount(accountId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        /*if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Current balance: " + account.getBalance());
        }*/

        account.setBalance(newBalance);
        accountRepository.save(account);
    }
}
