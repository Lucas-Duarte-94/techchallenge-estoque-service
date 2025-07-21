package com.tech_challenge.fiap_estoque_service.exception;

public class ReservationCannotBeCancelledException extends RuntimeException {
    public ReservationCannotBeCancelledException(String msg) {
        super(msg);
    }

}
