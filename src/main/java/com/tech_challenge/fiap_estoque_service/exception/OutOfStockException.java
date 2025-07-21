package com.tech_challenge.fiap_estoque_service.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException() {
        super("Um ou mais produtos não tem estoque suficiente.");
    }

    public OutOfStockException(String productSKU) {
        super("Estoque insuficiente para o produto: " + productSKU);
    }

}
