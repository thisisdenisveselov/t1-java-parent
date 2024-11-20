package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.model.dto.DataSourceErrorLogDto;

@Component
public class ErrorLogMapper {
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
