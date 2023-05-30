package com.example.demo.exception;

public class AlreadyRegisteredException extends RegistrationException{

    public AlreadyRegisteredException(String msg) {
        super(msg);
    }

    public AlreadyRegisteredException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
