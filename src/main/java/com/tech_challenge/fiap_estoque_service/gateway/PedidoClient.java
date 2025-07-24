package com.tech_challenge.fiap_estoque_service.gateway;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "pedido-service", url = "${pedido.service.url:http://localhost:8081/pedido}")
public interface PedidoClient {
    @PutMapping("/out-of-stock")
    void changeToClosedOutOfStock(String request);

    @PutMapping("/payment-fail")
    void changeToClosedPaymentFail(String request);

    @PutMapping("/success")
    void changeToClosedSuccess(String request);

    @PutMapping("/expried")
    void changeToClosedExpired(String request);
}
