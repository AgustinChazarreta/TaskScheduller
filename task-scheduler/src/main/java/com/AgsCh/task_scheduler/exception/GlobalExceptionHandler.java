package com.AgsCh.task_scheduler.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

        // =========================
        // ERRORES DE NEGOCIO (400)
        // =========================
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {

                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Business validation error",
                                List.of(ex.getMessage()));

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        // =========================
        // ERRORES NO CONTROLADOS (500)
        // =========================
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {

                // 🔴 FUNDAMENTAL: ver el error real en consola
                ex.printStackTrace();

                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                ex.getClass().getSimpleName(),
                                List.of(
                                                ex.getMessage() != null
                                                                ? ex.getMessage()
                                                                : "Unexpected error occurred"));

                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
