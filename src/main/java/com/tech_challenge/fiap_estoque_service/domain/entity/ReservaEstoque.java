package com.tech_challenge.fiap_estoque_service.domain.entity;

import java.time.LocalDateTime;

import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservaEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "product_sku")
    private String productSKU;

    private int quantidadeReservada;

    @Enumerated(EnumType.STRING)
    private ReservaStatus status;

    private String pedidoId;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt; // cria a possibilidade de tirar a reserva do pedido após algum determinado
                                     // período de tempo.
}
