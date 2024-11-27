package ru.t1.java.demo.t1_java_transaction_processor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckBlacklistRequest {
    private UUID clientId;
    private UUID accountId;
}
