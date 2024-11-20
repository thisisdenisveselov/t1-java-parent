package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.dto.TransactionDto;

@Component
public class DataSourceErrorLogMapper {
    public static DataSourceErrorLog toEntity(DataSourceErrorLogDto dto) {
        return DataSourceErrorLog.builder()
                .stackTraceText(dto.getStackTraceText())
                .message(dto.getMessage())
                .methodSignature(dto.getMethodSignature())
                .build();
    }

    public static DataSourceErrorLogDto toDto(DataSourceErrorLog entity) {
        return DataSourceErrorLogDto.builder()
                .stackTraceText(entity.getStackTraceText())
                .message(entity.getMessage())
                .methodSignature(entity.getMethodSignature())
                .build();
    }

}
