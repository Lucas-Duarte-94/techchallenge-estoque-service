package com.tech_challenge.fiap_estoque_service.usecase;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;
import com.tech_challenge.fiap_estoque_service.dto.EstoquePublicDTO;
import com.tech_challenge.fiap_estoque_service.dto.ItemPedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;
import com.tech_challenge.fiap_estoque_service.exception.ProductNotFoundException;
import com.tech_challenge.fiap_estoque_service.gateway.EstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstoqueUseCaseImplTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ReservaEstoqueUseCase reservaEstoqueUseCase;

    @Mock
    private UpdateStatusToCancelUseCase cancelStatusUseCase;

    @Mock
    private UpdateStatusToConfirmedUseCase confirmedStatusUseCase;

    @InjectMocks
    private EstoqueUseCaseImpl estoqueUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEstoqueByProductSKU_ShouldReturnEstoquePublicDTO_WhenProductExists() {
        String productSKU = "PROD001";
        Estoque estoque = new Estoque();
        estoque.setProductSKU(productSKU);
        estoque.setQuantidadeDisponivel(10);

        when(estoqueRepository.findById(productSKU)).thenReturn(Optional.of(estoque));

        EstoquePublicDTO result = estoqueUseCase.getEstoqueByProductSKU(productSKU);

        assertNotNull(result);
        assertEquals(productSKU, result.productSKU());
        assertEquals(10, result.quantidadeDisponivel());
        verify(estoqueRepository, times(1)).findById(productSKU);
    }

    @Test
    void getEstoqueByProductSKU_ShouldThrowProductNotFoundException_WhenProductDoesNotExist() {
        String productSKU = "PROD001";

        when(estoqueRepository.findById(productSKU)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> estoqueUseCase.getEstoqueByProductSKU(productSKU));
        verify(estoqueRepository, times(1)).findById(productSKU);
    }

    @Test
    void updateEstoque_ShouldCallReservaEstoqueUseCase() {
        ItemPedidoDTO item = new ItemPedidoDTO("PROD001", 2);
        PedidoDTO pedidoDTO = new PedidoDTO(Collections.singletonList(item), UUID.randomUUID().toString());

        estoqueUseCase.updateEstoque(pedidoDTO);

        verify(reservaEstoqueUseCase, times(1)).updateEstoque(pedidoDTO);
    }

    @Test
    void updateStatusToCancel_ShouldCallCancelStatusUseCase() {
        String pedidoId = UUID.randomUUID().toString();

        estoqueUseCase.updateStatusToCancel(pedidoId);

        verify(cancelStatusUseCase, times(1)).updateStatusToCancel(pedidoId);
    }

    @Test
    void updateStatusToConfirmed_ShouldCallConfirmedStatusUseCase() {
        String pedidoId = UUID.randomUUID().toString();

        estoqueUseCase.updateStatusToConfirmed(pedidoId);

        verify(confirmedStatusUseCase, times(1)).updateStatusToConfirmed(pedidoId);
    }
}
