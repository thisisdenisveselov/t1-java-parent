package ru.t1.java.demo.t1_java_transaction_processor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckBlacklistResponse {
    private Boolean blocked;
}
