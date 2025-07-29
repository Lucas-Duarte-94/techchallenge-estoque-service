package com.tech_challenge.fiap_estoque_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech_challenge.fiap_estoque_service.usecase.EstoqueUseCase;
import com.tech_challenge.fiap_estoque_service.dto.EstoquePublicDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoDTO;
import com.tech_challenge.fiap_estoque_service.dto.PedidoIdRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {
    private EstoqueUseCase estoqueUseCase;

    public EstoqueController(EstoqueUseCase estoqueUseCase) {
        this.estoqueUseCase = estoqueUseCase;
    }

    @GetMapping("/{product_sku}")
    public ResponseEntity<EstoquePublicDTO> getEstoqueByProductSKU(@PathVariable("product_sku") String productSKU) {
        var estoque = this.estoqueUseCase.getEstoqueByProductSKU(productSKU);
        return ResponseEntity.ok().body(estoque);
    }

    @PostMapping
    public ResponseEntity<Void> reserveStock(@RequestBody PedidoDTO pedidoDTO) {
        this.estoqueUseCase.updateEstoque(pedidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> changeToCancelStatus(@RequestBody PedidoIdRequestDTO pedidoIdRequestDTO) {
        this.estoqueUseCase.updateStatusToCancel(pedidoIdRequestDTO.pedidoId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> changeToConfirmedStatus(@RequestBody PedidoIdRequestDTO pedidoIdRequestDTO) {
        this.estoqueUseCase.updateStatusToConfirmed(pedidoIdRequestDTO.pedidoId());
        return ResponseEntity.noContent().build();
    }

}
