package com.tech_challenge.fiap_estoque_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("Um ou mais produtos não foram encontrados no estoque.");
    }

    public ProductNotFoundException(String productSKU) {
        super("Produto não encontrado: " + productSKU);
    }
}
