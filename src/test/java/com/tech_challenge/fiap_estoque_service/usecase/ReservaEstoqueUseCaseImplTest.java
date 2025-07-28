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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaEstoqueUseCaseImplTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ReservaEstoqueRepository reservaRepository;

    @InjectMocks
    private ReservaEstoqueUseCaseImpl reservaEstoqueUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateEstoque_ShouldSuccessfullyUpdateStockAndCreateReservations() {
        String productSKU1 = "PROD001";
        String productSKU2 = "PROD002";
        int initialQtd1 = 10;
        int initialQtd2 = 5;
        int reservedQtd1 = 2;
        int reservedQtd2 = 3;

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(initialQtd1);

        Estoque estoque2 = new Estoque();
        estoque2.setProductSKU(productSKU2);
        estoque2.setQuantidadeDisponivel(initialQtd2);

        ItemPedidoDTO item1 = new ItemPedidoDTO(productSKU1, reservedQtd1);
        ItemPedidoDTO item2 = new ItemPedidoDTO(productSKU2, reservedQtd2);
        String pedidoId = UUID.randomUUID().toString();
        PedidoDTO pedidoDTO = new PedidoDTO(Arrays.asList(item1, item2), pedidoId);

        when(estoqueRepository.findAllById(Arrays.asList(productSKU1, productSKU2)))
                .thenReturn(Arrays.asList(estoque1, estoque2));

        reservaEstoqueUseCase.updateEstoque(pedidoDTO);

        assertEquals(initialQtd1 - reservedQtd1, estoque1.getQuantidadeDisponivel());
        assertEquals(initialQtd2 - reservedQtd2, estoque2.getQuantidadeDisponivel());

        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());

        // Verify that reservations were created with correct status and pedidoId
        verify(reservaRepository, times(1)).saveAll(argThat(reservas -> {
            List<ReservaEstoque> reservaList = (List<ReservaEstoque>) reservas;
            assertEquals(2, reservaList.size());
            assertTrue(reservaList.stream().allMatch(r -> r.getStatus().equals(ReservaStatus.PENDENTE)));
            assertTrue(reservaList.stream().allMatch(r -> r.getPedidoId().equals(pedidoId)));
            return true;
        }));
    }

    @Test
    void updateEstoque_ShouldThrowProductNotFoundException_WhenProductSKUMismatch() {
        String productSKU1 = "PROD001";
        String productSKU2 = "PROD002";

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(10);

        ItemPedidoDTO item1 = new ItemPedidoDTO(productSKU1, 2);
        ItemPedidoDTO item2 = new ItemPedidoDTO(productSKU2, 3);
        PedidoDTO pedidoDTO = new PedidoDTO(Arrays.asList(item1, item2), UUID.randomUUID().toString());

        // Simulate only one product found, even though two were requested
        when(estoqueRepository.findAllById(Arrays.asList(productSKU1, productSKU2)))
                .thenReturn(Collections.singletonList(estoque1));

        assertThrows(ProductNotFoundException.class, () -> reservaEstoqueUseCase.updateEstoque(pedidoDTO));

        verify(estoqueRepository, times(1)).findAllById(anyList());
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateEstoque_ShouldThrowProductNotFoundException_WhenProductSKUDoesNotExist() {
        String productSKU1 = "PROD001";

        ItemPedidoDTO item1 = new ItemPedidoDTO(productSKU1, 2);
        PedidoDTO pedidoDTO = new PedidoDTO(Collections.singletonList(item1), UUID.randomUUID().toString());

        when(estoqueRepository.findAllById(Collections.singletonList(productSKU1)))
                .thenReturn(Collections.emptyList());

        assertThrows(ProductNotFoundException.class, () -> reservaEstoqueUseCase.updateEstoque(pedidoDTO));

        verify(estoqueRepository, times(1)).findAllById(anyList());
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateEstoque_ShouldThrowOutOfStockException_WhenQuantityIsInsufficient() {
        String productSKU1 = "PROD001";
        int initialQtd1 = 10;
        int reservedQtd1 = 12; // More than available

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(initialQtd1);

        ItemPedidoDTO item1 = new ItemPedidoDTO(productSKU1, reservedQtd1);
        PedidoDTO pedidoDTO = new PedidoDTO(Collections.singletonList(item1), UUID.randomUUID().toString());

        when(estoqueRepository.findAllById(Collections.singletonList(productSKU1)))
                .thenReturn(Collections.singletonList(estoque1));

        assertThrows(OutOfStockException.class, () -> reservaEstoqueUseCase.updateEstoque(pedidoDTO));

        verify(estoqueRepository, times(1)).findAllById(anyList());
        verify(estoqueRepository, never()).saveAll(anyList());
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    void updateEstoque_ShouldHandleMultipleItemsCorrectly() {
        String productSKU1 = "PROD001";
        String productSKU2 = "PROD002";
        int initialQtd1 = 10;
        int initialQtd2 = 5;
        int reservedQtd1 = 2;
        int reservedQtd2 = 3;

        Estoque estoque1 = new Estoque();
        estoque1.setProductSKU(productSKU1);
        estoque1.setQuantidadeDisponivel(initialQtd1);

        Estoque estoque2 = new Estoque();
        estoque2.setProductSKU(productSKU2);
        estoque2.setQuantidadeDisponivel(initialQtd2);

        ItemPedidoDTO item1 = new ItemPedidoDTO(productSKU1, reservedQtd1);
        ItemPedidoDTO item2 = new ItemPedidoDTO(productSKU2, reservedQtd2);
        String pedidoId = UUID.randomUUID().toString();
        PedidoDTO pedidoDTO = new PedidoDTO(Arrays.asList(item1, item2), pedidoId);

        when(estoqueRepository.findAllById(Arrays.asList(productSKU1, productSKU2)))
                .thenReturn(Arrays.asList(estoque1, estoque2));

        reservaEstoqueUseCase.updateEstoque(pedidoDTO);

        assertEquals(initialQtd1 - reservedQtd1, estoque1.getQuantidadeDisponivel());
        assertEquals(initialQtd2 - reservedQtd2, estoque2.getQuantidadeDisponivel());

        verify(estoqueRepository, times(1)).saveAll(anyList());
        verify(reservaRepository, times(1)).saveAll(anyList());
    }
}
