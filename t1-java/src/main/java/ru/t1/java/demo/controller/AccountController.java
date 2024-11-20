package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.util.AccountMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public AccountDto createAccount(@RequestBody AccountDto accountDto){
        return AccountMapper.toDto(accountService.createAccount(AccountMapper.toEntity(accountDto)));
    }

    @GetMapping
    public List<AccountDto> getAccounts(){
        return accountService.getAccounts().stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    @GetMapping(value = "/{id}")
    public AccountDto getAccountById(@PathVariable Long id){
        Account account = accountService.getAccount(id);
        return AccountMapper.toDto(account);
    }

    @PatchMapping("/{id}")
    public AccountDto updateAccount(@PathVariable Long id, @RequestBody AccountDto accountDto){
        return AccountMapper.toDto(accountService.updateAccount(id, AccountMapper.toEntity(accountDto)));
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id){
        accountService.deleteAccount(id);
    }
}
