package com.tech_challenge.fiap_estoque_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiapEstoqueServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiapEstoqueServiceApplication.class, args);
	}

}
