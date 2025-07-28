package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;
import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;
import com.tech_challenge.fiap_estoque_service.exception.ReservationCannotBeCancelledException;
import com.tech_challenge.fiap_estoque_service.exception.ReservationNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import com.tech_challenge.fiap_estoque_service.gateway.ReservaEstoqueRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class UpdateStatusToCancelUseCaseImplTest {

    @Mock
    private ReservaEstoqueRepository reservaRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @InjectMocks
    private UpdateStatusToCancelUseCaseImpl updateStatusToCancelUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateStatusToCancel_ShouldCancelPendingReservationsAndReturnStock() {
        String pedidoId = UUID.randomUUID().toString();
        String productSKU = "PROD001";
        int reservedQuantity = 5;

        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU)
                .quantidadeReservada(reservedQuantity)
                .status(ReservaStatus.PENDENTE)
                .expiresAt(LocalDateTime.now().plusMinutes(1))
                .build();

        Estoque estoque = new Estoque();
        estoque.setProductSKU(productSKU);
        estoque.setQuantidadeDisponivel(10);

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.singletonList(reserva));
        when(estoqueRepository.findAllById(Collections.singletonList(productSKU))).thenReturn(Collections.singletonList(estoque));

        updateStatusToCancelUseCase.updateStatusToCancel(pedidoId);

        assertEquals(ReservaStatus.CANCELADA, reserva.getStatus());
        assertEquals(10 + reservedQuantity, estoque.getQuantidadeDisponivel());
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldCancelExpiredReservationsAndReturnStock() {
        String pedidoId = UUID.randomUUID().toString();
        String productSKU = "PROD001";
        int reservedQuantity = 5;

        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU)
                .quantidadeReservada(reservedQuantity)
                .status(ReservaStatus.EXPIRADA)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        Estoque estoque = new Estoque();
        estoque.setProductSKU(productSKU);
        estoque.setQuantidadeDisponivel(10);

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.singletonList(reserva));
        when(estoqueRepository.findAllById(Collections.singletonList(productSKU))).thenReturn(Collections.singletonList(estoque));

        updateStatusToCancelUseCase.updateStatusToCancel(pedidoId);

        assertEquals(ReservaStatus.CANCELADA, reserva.getStatus());
        assertEquals(10 + reservedQuantity, estoque.getQuantidadeDisponivel());
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldThrowReservationNotFoundException_WhenNoReservationsFound() {
        String pedidoId = UUID.randomUUID().toString();

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.emptyList());

        assertThrows(ReservationNotFoundException.class, () -> updateStatusToCancelUseCase.updateStatusToCancel(pedidoId));
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldThrowReservationCannotBeCancelledException_WhenReservationIsFinalized() {
        String pedidoId = UUID.randomUUID().toString();
        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU("PROD001")
                .quantidadeReservada(5)
                .status(ReservaStatus.FINALIZADO)
                .build();

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.singletonList(reserva));

        assertThrows(ReservationCannotBeCancelledException.class, () -> updateStatusToCancelUseCase.updateStatusToCancel(pedidoId));
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldThrowReservationCannotBeCancelledException_WhenReservationIsConfirmed() {
        String pedidoId = UUID.randomUUID().toString();
        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU("PROD001")
                .quantidadeReservada(5)
                .status(ReservaStatus.CONFIRMADA)
                .build();

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.singletonList(reserva));

        assertThrows(ReservationCannotBeCancelledException.class, () -> updateStatusToCancelUseCase.updateStatusToCancel(pedidoId));
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldDoNothing_WhenAllReservationsAreAlreadyCancelled() {
        String pedidoId = UUID.randomUUID().toString();
        ReservaEstoque reserva = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU("PROD001")
                .quantidadeReservada(5)
                .status(ReservaStatus.CANCELADA)
                .build();

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Collections.singletonList(reserva));

        updateStatusToCancelUseCase.updateStatusToCancel(pedidoId);

        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldHandleMultipleReservationsForSameProductSKU() {
        String pedidoId = UUID.randomUUID().toString();
        String productSKU = "PROD001";

        ReservaEstoque reserva1 = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU)
                .quantidadeReservada(2)
                .status(ReservaStatus.PENDENTE)
                .build();
        ReservaEstoque reserva2 = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU)
                .quantidadeReservada(3)
                .status(ReservaStatus.EXPIRADA)
                .build();

        Estoque estoque = new Estoque();
        estoque.setProductSKU(productSKU);
        estoque.setQuantidadeDisponivel(10);

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Arrays.asList(reserva1, reserva2));
        when(estoqueRepository.findAllById(Collections.singletonList(productSKU))).thenReturn(Collections.singletonList(estoque));

        updateStatusToCancelUseCase.updateStatusToCancel(pedidoId);

        assertEquals(ReservaStatus.CANCELADA, reserva1.getStatus());
        assertEquals(ReservaStatus.CANCELADA, reserva2.getStatus());
        assertEquals(10 + 2 + 3, estoque.getQuantidadeDisponivel());
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateStatusToCancel_ShouldHandleMultipleReservationsForDifferentProductSKUs() {
        String pedidoId = UUID.randomUUID().toString();
        String productSKU1 = "PROD001";
        String productSKU2 = "PROD002";

        ReservaEstoque reserva1 = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU1)
                .quantidadeReservada(2)
                .status(ReservaStatus.PENDENTE)
                .build();
        ReservaEstoque reserva2 = ReservaEstoque.builder()
                .pedidoId(pedidoId)
                .productSKU(productSKU2)
                .quantidadeReservada(3)
                .status(ReservaStatus.EXPIRADA)
                .build();

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(10);

        Estoque estoque2 = new Estoque();
        estoque2.setProductSKU(productSKU2);
        estoque2.setQuantidadeDisponivel(15);

        when(reservaRepository.findByPedidoId(pedidoId)).thenReturn(Arrays.asList(reserva1, reserva2));
        when(estoqueRepository.findAllById(Arrays.asList(productSKU1, productSKU2))).thenReturn(Arrays.asList(estoque1, estoque2));

        updateStatusToCancelUseCase.updateStatusToCancel(pedidoId);

        assertEquals(ReservaStatus.CANCELADA, reserva1.getStatus());
        assertEquals(ReservaStatus.CANCELADA, reserva2.getStatus());
        assertEquals(10 + 2, estoque1.getQuantidadeDisponivel());
        assertEquals(15 + 3, estoque2.getQuantidadeDisponivel());
        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());
    }
}
