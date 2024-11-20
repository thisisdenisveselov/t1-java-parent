package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.producer.DataSourceErrorLogProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.service.DataSourceErrorLogService;
import ru.t1.java.demo.util.DataSourceErrorLogMapper;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Aspect
@Component
@Order(0)
public class LogAspect {
    private final DataSourceErrorLogService dataSourceErrorLogService;
    private final DataSourceErrorLogProducer dataSourceErrorLogProducer;
    private static final String HEADER_VALUE_DATA_SOURCE = "DATA_SOURCE";

    public LogAspect(DataSourceErrorLogService dataSourceErrorLogService, DataSourceErrorLogProducer dataSourceErrorLogProducer) {
        this.dataSourceErrorLogService = dataSourceErrorLogService;
        this.dataSourceErrorLogProducer = dataSourceErrorLogProducer;
    }

    @Pointcut("within(ru.t1.java.demo.*)")
    public void loggingMethods() {

    }

    @Before("@annotation(LogExecution)")
    @Order(1)
    public void logAnnotationBefore(JoinPoint joinPoint) {
        log.info("ASPECT BEFORE ANNOTATION: Call method: {}", joinPoint.getSignature().getName());
    }

//    @Before("execution(public * ru.t1.java.demo.service.ClientService.*(..))")
//    public void logBefore(JoinPoint joinPoint) {
//        log.error("ASPECT BEFORE: Call method: {}", joinPoint.getSignature().getName());
//    }

    @AfterThrowing(pointcut = "@annotation(LogException)")
    @Order(0)
    public void logExceptionAnnotation(JoinPoint joinPoint) {
        System.err.println("ASPECT EXCEPTION ANNOTATION: Logging exception: {}" + joinPoint.getSignature().getName());
    }

    @AfterReturning(
            pointcut = "@annotation(HandlingResult)",
            returning = "result")
    public void handleResult(JoinPoint joinPoint, List<Client> result) {
        log.info("В результате выполнения метода {}", joinPoint.getSignature().toShortString());
//        log.info("получен результат: {} ", result);
        log.info("Подробности: \n");

        result = isNull(result) ? List.of() : result;

    }

    @AfterThrowing(pointcut = "@annotation(LogDataSourceError)", throwing = "exception")
    public void logDataSourceError(JoinPoint joinPoint, Throwable exception) {
        log.info("ASPECT AFTER_THROWING ANNOTATION: Call method: {}", joinPoint.getSignature().getName());
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .message(exception.getMessage())
                .methodSignature(joinPoint.getSignature().toString())
                .stackTraceText(Arrays.toString(exception.getStackTrace()))
                .build();
        try {
            dataSourceErrorLogProducer.send(DataSourceErrorLogMapper.toDto(errorLog), HEADER_VALUE_DATA_SOURCE);
        } catch (Throwable throwable) {
            try {
                dataSourceErrorLogService.createLog(errorLog);
            } finally {
                log.error("Error while saving DataSourceErrorLog record: {}", errorLog.toString());
            }
        }
    }
}
