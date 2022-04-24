package com.myweather.exception;

public class AwsError extends Exception {
    public AwsError(final String message) {
        super(message);
    }
}
