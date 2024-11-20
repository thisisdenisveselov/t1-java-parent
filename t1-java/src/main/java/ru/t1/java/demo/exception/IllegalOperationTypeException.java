package ru.t1.java.demo.exception;

public class IllegalOperationTypeException extends RuntimeException {
    public IllegalOperationTypeException(String message) {
        super(message);
    }
}
