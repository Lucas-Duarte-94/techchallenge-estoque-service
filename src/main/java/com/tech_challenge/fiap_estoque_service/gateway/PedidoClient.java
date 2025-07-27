package com.tech_challenge.fiap_estoque_service.gateway;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

import com.tech_challenge.fiap_estoque_service.dto.PedidoRequestDTO;

@FeignClient(name = "pedido-service", url = "${pedido.service.url:http://localhost:8081/pedido}")
public interface PedidoClient {
    @PutMapping("/out-of-stock")
    void changeToClosedOutOfStock(String request);

    // @PutMapping("/payment-fail")
    // void changeToClosedPaymentFail(String request);

    @PutMapping("/success")
    void changeToClosedSuccess(PedidoRequestDTO request);

    @PutMapping("/expried")
    void changeToClosedExpired(PedidoRequestDTO request);
}
