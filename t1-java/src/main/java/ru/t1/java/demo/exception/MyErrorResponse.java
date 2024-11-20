package ru.t1.java.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
class MyErrorResponse {
    private HttpStatus status;
    private LocalDateTime timestamp;
    private String message;

    public MyErrorResponse() {
        timestamp = LocalDateTime.now();
    }

    public MyErrorResponse(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
}
