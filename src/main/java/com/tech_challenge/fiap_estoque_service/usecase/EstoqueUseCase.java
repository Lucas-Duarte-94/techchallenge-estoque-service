package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.dto.EstoquePublicDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;

public interface EstoqueUseCase {
    EstoquePublicDTO getEstoqueByProductSKU(String productSKU);

    void updateEstoque(PedidoDTO pedido);

    void updateStatusToCancel(String pedidoId);

    void updateStatusToConfirmed(String pedidoId);
}
