package com.AgsCh.task_scheduler.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message, Exception e) {
        super(message);
    }

    public BusinessException(String message) {
        super(message); 
    }
}
