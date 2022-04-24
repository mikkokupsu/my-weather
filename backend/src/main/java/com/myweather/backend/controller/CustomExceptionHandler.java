package com.myweather.backend.controller;

import com.myweather.exception.AwsError;
import com.myweather.exception.NotFound;
import com.myweather.exception.Unauthorized;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class CustomExceptionHandler {
    
    private static final Logger logger = LogManager.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler({Unauthorized.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    void unauthorized(Exception exception) {
        logger.warn("Unauthorized was thrown -> Respond with 401", exception);
    }

    @ExceptionHandler({AwsError.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    void awsError(Exception exception) {
        logger.error("AwsError was thrown -> Respond with 500", exception);
    }

    @ExceptionHandler({NotFound.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    void notFound(Exception exception) {
        logger.error("NotFound was thrown -> Respond with 404", exception);
    }
}
