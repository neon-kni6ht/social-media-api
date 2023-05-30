package com.example.demo.exception;

public class SocialLinkException extends RuntimeException{

    public SocialLinkException(String msg) {
        super(msg);
    }

    public SocialLinkException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
