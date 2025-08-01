package com.tech_challenge.fiap_estoque_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tech_challenge.fiap_estoque_service.exception.OutOfStockException;
import com.tech_challenge.fiap_estoque_service.exception.ProductNotFoundException;
import com.tech_challenge.fiap_estoque_service.exception.ReservationCannotBeCancelledException;
import com.tech_challenge.fiap_estoque_service.exception.ReservationNotFoundException;

@RestControllerAdvice
public class GlobalExceptionController {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<String> handleOutOfStockException(OutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<String> handleReservationNotFoundException(ReservationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReservationCannotBeCancelledException.class)
    public ResponseEntity<String> handleReservationCannotBeCancelledException(
            ReservationCannotBeCancelledException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
