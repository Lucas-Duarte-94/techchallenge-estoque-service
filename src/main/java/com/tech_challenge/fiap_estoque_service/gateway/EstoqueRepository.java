package com.tech_challenge.fiap_estoque_service.gateway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tech_challenge.fiap_estoque_service.domain.entity.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, String> {

}
