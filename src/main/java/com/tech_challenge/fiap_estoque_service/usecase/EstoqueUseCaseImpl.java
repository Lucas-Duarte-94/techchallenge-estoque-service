package com.tech_challenge.fiap_estoque_service.usecase;

import org.springframework.stereotype.Service;

import com.tech_challenge.fiap_estoque_service.dto.EstoquePublicDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;
import com.tech_challenge.fiap_estoque_service.exception.ProductNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;

@Service
public class EstoqueUseCaseImpl implements EstoqueUseCase {
    private EstoqueRepository estoqueRepository;
    private ReservaEstoqueUseCase reservaEstoqueUseCase;
    private UpdateStatusToCancelUseCase cancelStatusUseCase;
    private UpdateStatusToConfirmedUseCase confirmedStatusUseCase;

    public EstoqueUseCaseImpl(EstoqueRepository estoqueRepository, ReservaEstoqueUseCase reservaEstoqueUseCase,
            UpdateStatusToCancelUseCase cancelStatusUseCase, UpdateStatusToConfirmedUseCase confirmedStatusUseCase) {
        this.estoqueRepository = estoqueRepository;
        this.reservaEstoqueUseCase = reservaEstoqueUseCase;
        this.cancelStatusUseCase = cancelStatusUseCase;
        this.confirmedStatusUseCase = confirmedStatusUseCase;
    }

    @Override
    public EstoquePublicDTO getEstoqueByProductSKU(String productSKU) {
        var estoque = this.estoqueRepository.findById(productSKU).orElseThrow(ProductNotFoundException::new);
        return new EstoquePublicDTO(estoque.getProductSKU(), estoque.getQuantidadeDisponivel());
    }

    @Override
    public void updateEstoque(PedidoDTO pedido) {
        this.reservaEstoqueUseCase.updateEstoque(pedido);
    }

    @Override
    public void updateStatusToCancel(String pedidoId) {
        this.cancelStatusUseCase.updateStatusToCancel(pedidoId);
    }

    @Override
    public void updateStatusToConfirmed(String pedidoId) {
        this.confirmedStatusUseCase.updateStatusToConfirmed(pedidoId);
    }

}
