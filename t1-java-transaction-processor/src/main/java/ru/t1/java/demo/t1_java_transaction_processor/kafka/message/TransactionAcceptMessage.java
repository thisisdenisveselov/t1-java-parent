package ru.t1.java.demo.t1_java_transaction_processor.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.t1_java_transaction_processor.model.enums.OperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionAcceptMessage {
    private UUID clientId;
    private UUID accountId;
    private UUID transactionId;
    private BigDecimal transactionAmount;
    private OperationType operationType;
    private Instant timestamp;
    private BigDecimal accountBalance;
}
