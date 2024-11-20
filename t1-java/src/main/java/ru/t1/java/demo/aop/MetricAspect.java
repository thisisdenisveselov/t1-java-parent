package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.producer.MetricsProducer;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final MetricsProducer metricsProducer;
    private static final String HEADER_VALUE_METRICS = "METRICS";

    @Pointcut("within(ru.t1.java.demo.*)")
    public void loggingMethods() {
    }

    @Around("@annotation(Metric)")
    public Object checkIfExecutionTimeExceeds(ProceedingJoinPoint pJoinPoint) {
        log.info("ASPECT AROUND ANNOTATION: Call method: {}", pJoinPoint.getSignature().getName());

        MethodSignature signature = (MethodSignature) pJoinPoint.getSignature();
        Metric metricAnnotation = signature.getMethod().getAnnotation(Metric.class);
        long timeLimit = metricAnnotation.value();
        long beforeTime = System.currentTimeMillis();
        Object result = null;

        try {
            result = pJoinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Error while proceeding method: {}", pJoinPoint.getSignature(), throwable);
        }

        long afterTime = System.currentTimeMillis();
        long execTime = afterTime - beforeTime;
        String args = Arrays.stream(pJoinPoint.getArgs())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        if (args.isEmpty())
            args = "No parameters";

        if (execTime > timeLimit) {
            String message = String.format("Method: %s, Parameters: %s, Execution time: %d ms, Time limit: %d ms",
                    pJoinPoint.getSignature(), args, execTime, timeLimit);
            try {
                metricsProducer.send(message, HEADER_VALUE_METRICS);
                log.info("Alert sent to Kafka: {}", message);
            } catch (Throwable throwable) {
                log.error("Error while sending message: {} to Kafka", message, throwable);
            }
        }

        return result;
    }


}
