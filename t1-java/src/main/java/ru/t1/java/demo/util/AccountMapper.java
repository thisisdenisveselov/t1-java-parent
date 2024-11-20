package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.Account;

@Component
public class AccountMapper {
    public static Account toEntity(AccountDto dto) {
        return Account.builder()
                .accountId(dto.getAccountId())
                .type(dto.getType())
                .balance(dto.getBalance())
                .status(dto.getStatus())
                .frozenAmount(dto.getFrozenAmount())
                .owner(ClientMapper.toEntity(dto.getOwner()))
                .build();
    }

    public static AccountDto toDto(Account entity) {
        return AccountDto.builder()
                .accountId(entity.getAccountId())
                .type(entity.getType())
                .balance(entity.getBalance())
                .status(entity.getStatus())
                .frozenAmount(entity.getFrozenAmount())
                .owner(ClientMapper.toDto(entity.getOwner()))
                .build();
    }

}
