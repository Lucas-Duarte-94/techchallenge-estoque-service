package com.tech_challenge.fiap_estoque_service.scheduler;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.PedidoRequestDTO;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import com.tech_challenge.fiap_estoque_service.gateway.PedidoClient;
import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaExpirationSchedulerTest {

    @Mock
    private ReservaEstoqueRepository reservaRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private ReservaExpirationScheduler reservaExpirationScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cancelarReservasExpiradas_ShouldDoNothingWhenNoExpiredReservations() {
        when(reservaRepository.findByStatusAndExpiresAtBefore(eq(ReservaStatus.PENDENTE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reservaExpirationScheduler.cancelarReservasExpiradas();

        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
        verify(pedidoClient, never()).changeToClosedExpired(any(PedidoRequestDTO.class));
    }

    @Test
    void cancelarReservasExpiradas_ShouldUpdateStockAndReservationsAndCallPedidoClient() {
        String pedidoId1 = UUID.randomUUID().toString();
        String pedidoId2 = UUID.randomUUID().toString();
        String productSKU1 = "PROD001";
        String productSKU2 = "PROD002";

        ReservaEstoque reserva1 = ReservaEstoque.builder()
                .pedidoId(pedidoId1)
                .productSKU(productSKU1)
                .quantidadeReservada(2)
                .status(ReservaStatus.PENDENTE)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .build();

        ReservaEstoque reserva2 = ReservaEstoque.builder()
                .pedidoId(pedidoId2)
                .productSKU(productSKU2)
                .quantidadeReservada(3)
                .status(ReservaStatus.PENDENTE)
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .build();

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(10);

        Estoque estoque2 = new Estoque();
        estoque2.setProductSKU(productSKU2);
        estoque2.setQuantidadeDisponivel(15);

        when(reservaRepository.findByStatusAndExpiresAtBefore(eq(ReservaStatus.PENDENTE), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(reserva1, reserva2));
        when(estoqueRepository.findAllById(Arrays.asList(productSKU1, productSKU2)))
                .thenReturn(Arrays.asList(estoque1, estoque2));

        reservaExpirationScheduler.cancelarReservasExpiradas();

        // Verify stock updates
        assertEquals(10 + 2, estoque1.getQuantidadeDisponivel());
        assertEquals(15 + 3, estoque2.getQuantidadeDisponivel());

        // Verify reservation status updates
        assertEquals(ReservaStatus.EXPIRADA, reserva1.getStatus());
        assertEquals(ReservaStatus.EXPIRADA, reserva2.getStatus());

        // Verify repositories save calls
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());

        // Verify pedidoClient calls
        verify(pedidoClient, times(1)).changeToClosedExpired(new PedidoRequestDTO(pedidoId1));
        verify(pedidoClient, times(1)).changeToClosedExpired(new PedidoRequestDTO(pedidoId2));
    }

    @Test
    void cancelarReservasExpiradas_ShouldHandleFeignException() {
        String pedidoId = UUID.randomUUID().toString();
        String productSKU = "PROD001";

        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU)
                .quantidadeReservada(2)
                .status(ReservaStatus.PENDENTE)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Estoque estoque = new Estoque();
        estoque.setProductSKU(productSKU);
        estoque.setQuantidadeDisponivel(10);

        when(reservaRepository.findByStatusAndExpiresAtBefore(eq(ReservaStatus.PENDENTE), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(reserva));
        when(estoqueRepository.findAllById(Collections.singletonList(productSKU)))
                .thenReturn(Collections.singletonList(estoque));
        doThrow(FeignException.class).when(pedidoClient).changeToClosedExpired(any(PedidoRequestDTO.class));

        reservaExpirationScheduler.cancelarReservasExpiradas();

        // Verify stock updates still happen
        assertEquals(10 + 2, estoque.getQuantidadeDisponivel());
        assertEquals(ReservaStatus.EXPIRADA, reserva.getStatus());

        // Verify repositories save calls still happen
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());

        // Verify pedidoClient was still attempted
        verify(pedidoClient, times(1)).changeToClosedExpired(any(PedidoRequestDTO.class));
    }
}
