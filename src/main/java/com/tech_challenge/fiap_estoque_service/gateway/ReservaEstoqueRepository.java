package com.tech_challenge.fiap_estoque_service.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tech_challenge.fiap_estoque_service.domain.entity.ReservaEstoque;

import com.tech_challenge.fiap_estoque_service.dto.ReservaStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaEstoqueRepository extends JpaRepository<ReservaEstoque, String> {

    List<ReservaEstoque> findByStatusAndExpiresAtBefore(ReservaStatus status, LocalDateTime now);
    List<ReservaEstoque> findByPedidoId(String pedidoId);
}
