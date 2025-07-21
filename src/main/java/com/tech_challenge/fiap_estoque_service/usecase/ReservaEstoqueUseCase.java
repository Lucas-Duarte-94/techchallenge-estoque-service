package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;

public interface ReservaEstoqueUseCase {
    void updateEstoque(PedidoDTO pedidoDTO);
}
