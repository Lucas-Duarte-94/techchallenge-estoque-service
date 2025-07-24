package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.exception.ReservationCannotBeCancelledException;
import com.tech_challenge.fiap_estoque_service.exception.ReservationNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;

import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UpdateStatusToCancelUseCaseImpl implements UpdateStatusToCancelUseCase {
    private final Logger logger = LoggerFactory.getLogger(UpdateStatusToCancelUseCaseImpl.class);

    private final ReservaEstoqueRepository reservaRepository;
    private final EstoqueRepository estoqueRepository;

    public UpdateStatusToCancelUseCaseImpl(ReservaEstoqueRepository reservaRepository,
            EstoqueRepository estoqueRepository) {
        this.reservaRepository = reservaRepository;
        this.estoqueRepository = estoqueRepository;

    }

    @Override
    @Transactional
    public void updateStatusToCancel(String pedidoId) {
        logger.info("metodo findByPedidoId " + pedidoId);
        List<ReservaEstoque> reservas = reservaRepository.findByPedidoId(pedidoId);
        logger.info("reservas: " + reservas);

        if (reservas.isEmpty()) {
            throw new ReservationNotFoundException("Reserva não encontrada para o pedido: " + pedidoId);
        }

        boolean anyFinalized = reservas.stream()
                .anyMatch(reserva -> reserva.getStatus() == ReservaStatus.FINALIZADO
                        || reserva.getStatus() == ReservaStatus.CONFIRMADA);

        if (anyFinalized) {
            throw new ReservationCannotBeCancelledException(
                    "Não é possível cancelar reservas com status FINALIZADO ou CONFIRMADA para o pedido");
        }

        boolean allCancelled = reservas.stream()
                .allMatch(reserva -> reserva.getStatus() == ReservaStatus.CANCELADA);
        if (allCancelled) {
            return;
        }

        Map<String, Integer> quantidadesParaDevolverDisponivel = reservas.stream()
                .filter(reserva -> reserva.getStatus() == ReservaStatus.PENDENTE
                        || reserva.getStatus() == ReservaStatus.EXPIRADA)
                .collect(Collectors.groupingBy(ReservaEstoque::getProductSKU,
                        Collectors.summingInt(ReservaEstoque::getQuantidadeReservada)));

        // Map<String, Integer> quantidadesParaDevolverReal = reservas.stream()
        // .filter(reserva -> reserva.getStatus() == ReservaStatus.CONFIRMADA)
        // .collect(Collectors.groupingBy(ReservaEstoque::getProductSKU,
        // Collectors.summingInt(ReservaEstoque::getQuantidadeReservada)));

        List<String> skusParaAtualizar = reservas.stream()
                .map(ReservaEstoque::getProductSKU)
                .distinct()
                .toList();
        List<Estoque> estoquesParaAtualizar = estoqueRepository.findAllById(skusParaAtualizar);

        for (Estoque estoque : estoquesParaAtualizar) {
            quantidadesParaDevolverDisponivel.computeIfPresent(estoque.getProductSKU(), (sku, qtd) -> {
                estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + qtd);
                return qtd;
            });

            // quantidadesParaDevolverReal.computeIfPresent(estoque.getProductSKU(), (sku,
            // qtd) -> {
            // estoque.setQuantidadeReal(estoque.getQuantidadeReal() + qtd);
            // return qtd;
            // });
            estoque.setUpdatedAt(LocalDateTime.now());
        }

        for (ReservaEstoque reserva : reservas) {
            reserva.setStatus(ReservaStatus.CANCELADA);
        }

        estoqueRepository.saveAll(estoquesParaAtualizar);
        reservaRepository.saveAll(reservas);
    }
}