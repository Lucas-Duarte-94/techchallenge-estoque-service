package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.ItemPedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.exception.OutOfStockException;
import com.tech_challenge.fiap_estoque_service.exception.ProductNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReservaEstoqueUseCaseImpl implements ReservaEstoqueUseCase {

    private final EstoqueRepository estoqueRepository;
    private final ReservaEstoqueRepository reservaRepository;

    public ReservaEstoqueUseCaseImpl(EstoqueRepository estoqueRepository, ReservaEstoqueRepository reservaRepository) {
        this.estoqueRepository = estoqueRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional
    public void updateEstoque(PedidoDTO pedidoDTO) {
        List<String> productSKUs = pedidoDTO.pedidos().stream()
                .map(ItemPedidoDTO::productSKU)
                .toList();
        List<Estoque> estoques = this.estoqueRepository.findAllById(productSKUs);

        validarDisponibilidade(estoques, pedidoDTO.pedidos());
        criarReservasEAtualizarEstoque(estoques, pedidoDTO);

        this.estoqueRepository.saveAll(estoques);
    }

    private void validarDisponibilidade(List<Estoque> estoques, List<ItemPedidoDTO> itensPedido) {
        if (estoques.size() != itensPedido.size()) {
            throw new ProductNotFoundException();
        }

        for (ItemPedidoDTO item : itensPedido) {
            Estoque estoqueDoItem = estoques.stream()
                    .filter(e -> e.getProductSKU().equals(item.productSKU()))
                    .findFirst()
                    .orElseThrow(() -> new ProductNotFoundException(item.productSKU()));

            if (estoqueDoItem.getQuantidadeDisponivel() < item.qtd()) {
                throw new OutOfStockException();
            }
        }
    }

    private void criarReservasEAtualizarEstoque(List<Estoque> estoques, PedidoDTO pedidoDTO) {
        List<ReservaEstoque> novasReservas = new ArrayList<>();

        for (ItemPedidoDTO item : pedidoDTO.pedidos()) {
            Estoque estoqueDoItem = estoques.stream()
                    .filter(e -> e.getProductSKU().equals(item.productSKU()))
                    .findFirst().get();

            estoqueDoItem.setQuantidadeDisponivel(estoqueDoItem.getQuantidadeDisponivel() - item.qtd());
            estoqueDoItem.setUpdatedAt(LocalDateTime.now());

            ReservaEstoque novaReserva = ReservaEstoque.builder()
                    .pedidoId(pedidoDTO.pedidoId())
                    .productSKU(item.productSKU())
                    .quantidadeReservada(item.qtd())
                    .status(ReservaStatus.PENDENTE)
                    .expiresAt(LocalDateTime.now().plusMinutes(1)) // 1 minuto para facilitar o teste de mudança de
                                                                   // status
                    .build();

            novasReservas.add(novaReserva);
        }

        this.reservaRepository.saveAll(novasReservas);
    }
}