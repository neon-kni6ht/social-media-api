package com.example.demo.exception;

public class InvalidCredentialsException extends RegistrationException{

    public InvalidCredentialsException(String msg) {
        super(msg);
    }

    public InvalidCredentialsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
