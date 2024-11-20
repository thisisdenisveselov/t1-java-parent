package ru.t1.java.demo.model.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataSourceErrorLogDto {
    private String stackTraceText;
    private String message;
    private String methodSignature;
}
