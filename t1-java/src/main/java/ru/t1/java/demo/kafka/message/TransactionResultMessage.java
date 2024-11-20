package ru.t1.java.demo.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResultMessage {
    private TransactionStatus status;
    private UUID accountId;
    private UUID transactionId;
    private OperationType operationType;
    private BigDecimal transactionAmount;
}
