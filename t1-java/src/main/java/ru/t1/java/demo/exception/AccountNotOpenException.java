package ru.t1.java.demo.exception;

public class AccountNotOpenException extends RuntimeException {
    public AccountNotOpenException(String message) {
        super(message);
    }
}
