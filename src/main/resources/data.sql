INSERT INTO usuarios (id, username, password, nombre_completo, rol) VALUES
(1, 'cgomez', '$2a$10$aoScDQEO.4uKepA6cBbkZugy26XEvT1Pa/fD1aemCyaO0h0QcWf0S', 'Carlos Gómez', 'ADMIN'),
(2, 'lperez', '$2a$10$aL2N2DcQAGg8GmrGmYD8mu6m7otjjoXEAAhu7cLFW7TTptpSWUUTG', 'Laura Pérez', 'ENCARGADO'),
(3, 'matorres', '$2a$10$W4moBz5pvqhMCiNhuNMCgeFk3xXl4oV5/IvuZJh/laXaGP7eS.zCm', 'Mariana Torres', 'OPERADOR'),
(4, 'jhernandez', '$2a$10$sctikZu/tKgmcMkVrbwYe.J4WRrYeVU/auQsW3OQHjcD/04L2VYda', 'Julián Hernández', 'ENCARGADO'),
(5, 'arios', '$2a$10$nanvYZL/aLyj.ThPkDwym.nQi1N0zJTPAhhh.esEbUy9gQpYUxgw6', 'Andrés Ríos', 'OPERADOR');

INSERT INTO bodegas (id, nombre, ubicacion, capacidad, encargado_id) VALUES
(1, 'Bodega Central', 'Zona Industrial Norte, Medellín', 5000, 2),
(2, 'Bodega Norte', 'Km 12 Vía Bello, Medellín', 3000, 2),
(3, 'Bodega Sur', 'Parque Logístico del Sur, Itagüí', 4500, 4),
(4, 'Bodega Occidente', 'Av. 80 #45-12, Medellín', 2500, 4),
(5, 'Bodega Oriente', 'Aeropuerto José María Córdova, Rionegro', 6000, 4);


INSERT INTO productos (id, nombre, categoria, stock, precio, bodega_id) VALUES
(1, 'Monitor LG 27"', 'Electrónica', 35, 950000.00, 1),
(2, 'Teclado Mecánico Redragon', 'Periféricos', 80, 220000.00, 2),
(3, 'Mouse Logitech G Pro', 'Periféricos', 60, 180000.00, 3),
(4, 'Silla Gamer Cougar Armor', 'Muebles', 15, 1250000.00, 4),
(5, 'Disco SSD 1TB Samsung', 'Almacenamiento', 40, 380000.00, 5);


INSERT INTO movimientos_inventario (id, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(1, 'ENTRADA', 1, NULL, 1),         
(2, 'SALIDA', 2, 2, NULL),          
(3, 'TRANSFERENCIA', 5, 1, 3),      
(4, 'ENTRADA', 3, NULL, 4),         
(5, 'SALIDA', 4, 5, NULL);          


INSERT INTO detalle_movimiento (id, movimiento_id, producto_id, cantidad)
VALUES
(1, 1, 1, 20),  -- Entrada de 20 Monitores LG a Bodega Central
(2, 1, 2, 15),
(3, 2, 3, 10),  -- Salida de 10 Mouse Logitech
(4, 3, 4, 5),   -- Transferencia de 5 Sillas Gamer de Central a Sur
(5, 3, 5, 10);

INSERT INTO auditoria (id, tipo_operacion, usuario_id, entidad_afectada, valor_anterior, valor_nuevo) VALUES
(1, 'INSERT', 1, 'Bodega', NULL, '{"id":1,"nombre":"Bodega Central","capacidad":5000}'),
(2, 'UPDATE', 2, 'Producto', '{"id":2,"stock":80}', '{"id":2,"stock":75}'),
(3, 'DELETE', 5, 'MovimientoInventario', '{"id":3,"tipo":"TRANSFERENCIA"}', NULL),
(4, 'INSERT', 3, 'Producto', NULL, '{"id":6,"nombre":"Laptop Dell","precio":3500000}'),
(5, 'UPDATE', 4, 'Bodega', '{"id":5,"encargado":"Julián Hernández"}', '{"id":5,"encargado":"Luis Pardo"}');

