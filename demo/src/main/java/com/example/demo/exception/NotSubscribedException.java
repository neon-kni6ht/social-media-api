package com.example.demo.exception;

public class NotSubscribedException extends SocialLinkException{

    public NotSubscribedException(String msg) {
        super(msg);
    }

    public NotSubscribedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
