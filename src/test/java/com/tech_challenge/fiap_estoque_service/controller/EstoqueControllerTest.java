package com.tech_challenge.fiap_estoque_service.controller;

import com.tech_challenge.fiap_estoque_service.dto.EstoquePublicDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.ItemPedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoIdRequestDTO;
import com.tech_challenge.fiap_estoque_service.usecase.EstoqueUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EstoqueControllerTest {

    @Mock
    private EstoqueUseCase estoqueUseCase;

    @InjectMocks
    private EstoqueController estoqueController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEstoqueByProductSKU_ShouldReturnEstoquePublicDTO() {
        String productSKU = "PROD001";
        EstoquePublicDTO expectedDTO = new EstoquePublicDTO(productSKU, 10);
        when(estoqueUseCase.getEstoqueByProductSKU(productSKU)).thenReturn(expectedDTO);

        ResponseEntity<EstoquePublicDTO> response = estoqueController.getEstoqueByProductSKU(productSKU);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody());
        verify(estoqueUseCase, times(1)).getEstoqueByProductSKU(productSKU);
    }

    @Test
    void reserveStock_ShouldReturnNoContent() {
        ItemPedidoDTO item = new ItemPedidoDTO("PROD001", 2);
        PedidoDTO pedidoDTO = new PedidoDTO(Collections.singletonList(item), UUID.randomUUID().toString());

        ResponseEntity<Void> response = estoqueController.reserveStock(pedidoDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(estoqueUseCase, times(1)).updateEstoque(pedidoDTO);
    }

    @Test
    void changeToCancelStatus_ShouldReturnNoContent() {
        UUID pedidoId = UUID.randomUUID();
        PedidoIdRequestDTO pedidoIdRequestDTO = new PedidoIdRequestDTO(pedidoId.toString());

        ResponseEntity<Void> response = estoqueController.changeToCancelStatus(pedidoIdRequestDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(estoqueUseCase, times(1)).updateStatusToCancel(pedidoId.toString());
    }

    @Test
    void changeToConfirmedStatus_ShouldReturnNoContent() {
        UUID pedidoId = UUID.randomUUID();
        PedidoIdRequestDTO pedidoIdRequestDTO = new PedidoIdRequestDTO(pedidoId.toString());

        ResponseEntity<Void> response = estoqueController.changeToConfirmedStatus(pedidoIdRequestDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(estoqueUseCase, times(1)).updateStatusToConfirmed(pedidoId.toString());
    }
}
