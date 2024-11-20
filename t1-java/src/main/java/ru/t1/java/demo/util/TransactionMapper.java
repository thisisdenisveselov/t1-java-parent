package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;

@Component
public class TransactionMapper {
    public static Transaction toEntity(TransactionDto dto) {
        return Transaction.builder()
                .transactionId(dto.getTransactionId())
                .amount(dto.getAmount())
                .timestamp(dto.getTimestamp())
                .status(dto.getStatus())
                .operationType(dto.getOperationType())
                .account(Account.builder()
                        .id(dto.getAccount().getId())
                        .accountId(dto.getAccount().getAccountId())
                        .build())
                .build();
    }

    public static TransactionDto toDto(Transaction entity) {
        return TransactionDto.builder()
                .transactionId(entity.getTransactionId())
                .amount(entity.getAmount())
                .timestamp(entity.getTimestamp())
                .status(entity.getStatus())
                .operationType(entity.getOperationType())
                .account(AccountMapper.toDto(entity.getAccount()))
                .build();
    }

}
