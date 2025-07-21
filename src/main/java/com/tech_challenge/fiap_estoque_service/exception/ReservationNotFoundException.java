package com.tech_challenge.fiap_estoque_service.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String msg) {
        super(msg);
    }
}
