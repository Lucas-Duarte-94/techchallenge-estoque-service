CREATE TABLE estoque (
    product_sku VARCHAR(255) NOT NULL,
    quantidade_disponivel INT NOT NULL,
    quantidade_real INT NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (product_sku)
);

CREATE TABLE reserva_estoque (
    id VARCHAR(36) NOT NULL,
    product_sku VARCHAR(255) NOT NULL,
    quantidade_reservada INT NOT NULL,
    status VARCHAR(255)NOT NULL,
    pedido_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
