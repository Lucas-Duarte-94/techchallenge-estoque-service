package com.tech_challenge.fiap_estoque_service.scheduler;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.PedidoRequestDTO;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import com.tech_challenge.fiap_estoque_service.gateway.PedidoClient;
import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservaExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservaExpirationScheduler.class);
    private final ReservaEstoqueRepository reservaRepository;
    private final EstoqueRepository estoqueRepository;
    private final PedidoClient pedidoClient;

    public ReservaExpirationScheduler(ReservaEstoqueRepository reservaRepository, EstoqueRepository estoqueRepository,
            PedidoClient pedidoClient) {
        this.reservaRepository = reservaRepository;
        this.estoqueRepository = estoqueRepository;
        this.pedidoClient = pedidoClient;
    }

    @Scheduled(fixedRate = 2 * 60000) // Roda a cada 2 minutos.
    @Transactional
    public void cancelarReservasExpiradas() {
        logger.info("Iniciando verificação de reservas expiradas...");

        List<ReservaEstoque> reservasExpiradas = reservaRepository
                .findByStatusAndExpiresAtBefore(ReservaStatus.PENDENTE, LocalDateTime.now());

        if (reservasExpiradas.isEmpty()) {
            logger.info("Nenhuma reserva expirada encontrada.");
            return;
        }

        logger.info("Encontradas {} reservas expiradas para cancelar.", reservasExpiradas.size());

        Map<String, Integer> quantidadesARevolver = reservasExpiradas.stream()
                .collect(Collectors.groupingBy(ReservaEstoque::getProductSKU,
                        Collectors.summingInt(ReservaEstoque::getQuantidadeReservada)));

        List<String> skusParaAtualizar = quantidadesARevolver.keySet().stream().toList();
        List<Estoque> estoquesParaAtualizar = estoqueRepository.findAllById(skusParaAtualizar);

        for (Estoque estoque : estoquesParaAtualizar) {
            int quantidadeDevolvida = quantidadesARevolver.get(estoque.getProductSKU());
            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + quantidadeDevolvida);
            estoque.setUpdatedAt(LocalDateTime.now());
            logger.debug("Devolvendo {} itens para o estoque do produto {}", quantidadeDevolvida,
                    estoque.getProductSKU());
        }

        for (ReservaEstoque reserva : reservasExpiradas) {
            reserva.setStatus(ReservaStatus.EXPIRADA);
            updatePedidoService(reserva.getPedidoId());
        }

        estoqueRepository.saveAll(estoquesParaAtualizar);
        reservaRepository.saveAll(reservasExpiradas);

        logger.info("Processo de cancelamento de reservas expiradas concluído.");
    }

    private void updatePedidoService(String pedidoId) {
        try {
            this.pedidoClient.changeToClosedExpired(new PedidoRequestDTO(pedidoId));
        } catch (FeignException ex) {
            logger.error("Erro ao chamar o serviço de pedido.\n - Stacktrace: {}", ex);
        }
    }
}
