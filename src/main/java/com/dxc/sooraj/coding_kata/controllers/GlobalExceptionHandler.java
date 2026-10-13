package com.dxc.sooraj.coding_kata.controllers;

import com.dxc.sooraj.coding_kata.dto.ApiErrorResponse;
import com.dxc.sooraj.coding_kata.exceptions.AccountNotFoundException;
import com.dxc.sooraj.coding_kata.exceptions.InsufficientFundsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(
            AccountNotFoundException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientFunds(
            InsufficientFundsException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentUpdate(
            OptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Account was updated by another request. Please retry",
                request.getRequestURI());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request) {
        String message = exception instanceof IllegalArgumentException
                ? exception.getMessage() : "Request data is invalid";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path) {
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
        return ResponseEntity.status(status).body(error);
    }
}
