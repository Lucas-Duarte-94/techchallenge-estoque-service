package com.tech_challenge.fiap_estoque_service.dto;

public enum ReservaStatus {
    PENDENTE,
    CONFIRMADA,
    EXPIRADA,
    CANCELADA,
    FINALIZADO
}

// Status FINALIZADO será para quando o pedido ja tiver sido entregue ao cliente
// e não for possivel devolver