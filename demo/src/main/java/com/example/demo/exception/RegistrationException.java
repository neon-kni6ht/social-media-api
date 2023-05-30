package com.example.demo.exception;

public class RegistrationException extends RuntimeException{

    public RegistrationException(String msg) {
        super(msg);
    }

    public RegistrationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
