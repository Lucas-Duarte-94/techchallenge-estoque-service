package com.tech_challenge.fiap_estoque_service.dto;

import java.util.List;

public record PedidoDTO(
        List<ItemPedidoDTO> pedidos,
        String pedidoId) {

}
