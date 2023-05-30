package com.example.demo.exception;

public class NotFriendsException extends SocialLinkException{

    public NotFriendsException(String msg) {
        super(msg);
    }

    public NotFriendsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
