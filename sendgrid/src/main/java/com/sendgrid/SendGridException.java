package com.sendgrid;

public class SendGridException extends Exception {
    public SendGridException(String errorMessage) {
        super(errorMessage);
    }
}