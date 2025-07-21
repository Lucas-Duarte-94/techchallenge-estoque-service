package com.tech_challenge.fiap_estoque_service.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Estoque {
    @Id
    @Column(name = "product_sku")
    private String productSKU;

    private int quantidadeDisponivel;

    private int quantidadeReal;

    private LocalDateTime updatedAt;
}
