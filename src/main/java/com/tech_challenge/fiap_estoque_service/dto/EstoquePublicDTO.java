package com.tech_challenge.fiap_estoque_service.dto;

public record EstoquePublicDTO(
        String productSKU,
        int quantidadeDisponivel) {
}
