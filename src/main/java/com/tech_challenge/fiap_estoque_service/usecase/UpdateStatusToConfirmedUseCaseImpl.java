package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.exception.ReservationNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import com.tech_challenge.fiap_estoque_service.gateway.PedidoClient;
import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UpdateStatusToConfirmedUseCaseImpl implements UpdateStatusToConfirmedUseCase {
    private final Logger logger = LoggerFactory.getLogger(UpdateStatusToConfirmedUseCaseImpl.class);

    private final ReservaEstoqueRepository reservaRepository;
    private final EstoqueRepository estoqueRepository;

    public UpdateStatusToConfirmedUseCaseImpl(ReservaEstoqueRepository reservaRepository,
            EstoqueRepository estoqueRepository) {
        this.reservaRepository = reservaRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Override
    @Transactional
    public void updateStatusToConfirmed(String pedidoId) {
        List<ReservaEstoque> reservas = reservaRepository.findByPedidoId(pedidoId);

        if (reservas.isEmpty()) {
            throw new ReservationNotFoundException("Reserva não encontrada para o pedido: " + pedidoId);
        }

        boolean allPending = reservas.stream()
                .allMatch(reserva -> reserva.getStatus() == ReservaStatus.PENDENTE);

        if (!allPending) {
            boolean alreadyConfirmed = reservas.stream()
                    .allMatch(reserva -> reserva.getStatus() == ReservaStatus.CONFIRMADA);
            if (alreadyConfirmed) {
                return;
            }
            throw new IllegalStateException(
                    "Algumas reservas para o pedido " + pedidoId + " não estão no status PENDENTE.");
        }

        Map<String, Integer> quantidadesParaBaixa = reservas.stream()
                .collect(Collectors.groupingBy(ReservaEstoque::getProductSKU,
                        Collectors.summingInt(ReservaEstoque::getQuantidadeReservada)));

        List<String> skusParaAtualizar = quantidadesParaBaixa.keySet().stream().toList();
        List<Estoque> estoquesParaAtualizar = estoqueRepository.findAllById(skusParaAtualizar);

        for (Estoque estoque : estoquesParaAtualizar) {
            int quantidadeReservada = quantidadesParaBaixa.get(estoque.getProductSKU());
            estoque.setQuantidadeReal(estoque.getQuantidadeReal() - quantidadeReservada);
            estoque.setUpdatedAt(LocalDateTime.now());
        }

        for (ReservaEstoque reserva : reservas) {
            reserva.setStatus(ReservaStatus.CONFIRMADA);
        }

        estoqueRepository.saveAll(estoquesParaAtualizar);
        reservaRepository.saveAll(reservas);

        // this.updatePedidoService(pedidoId);
    }

    // private void updatePedidoService(String pedidoId) {
    // try {
    // this.pedidoClient.changeToClosedSuccess(pedidoId);
    // } catch (FeignException ex) {
    // logger.error("Erro ao chamar o serviço de pedido.\n - Stacktrace: {}", ex);
    // }
    // }
}