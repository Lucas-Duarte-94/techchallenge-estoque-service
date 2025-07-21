INSERT INTO reserva_estoque (id, product_sku, quantidade_reservada, status, pedido_id, expires_at)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'cam-masc-ver-g', 5, 'PENDENTE', 'pedido-teste-123', DATEADD('MINUTE', 1, NOW()));

UPDATE estoque
SET quantidade_disponivel = quantidade_disponivel - 5
WHERE product_sku = 'cam-masc-ver-g';
