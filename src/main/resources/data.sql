INSERT INTO bodegas (nombre, ubicacion, capacidad, encargado_id) VALUES
('Bodega Central', 'Zona Industrial Norte, Medellín', 5000, 2),
('Bodega Norte', 'Km 12 Vía Bello, Medellín', 3000, 2),
('Bodega Sur', 'Parque Logístico del Sur, Itagüí', 4500, 4),
('Bodega Occidente', 'Av. 80 #45-12, Medellín', 2500, 4),
('Bodega Oriente', 'Aeropuerto José María Córdova, Rionegro', 6000, 4);

INSERT INTO productos (nombre, categoria, stock, precio, bodega_id) VALUES
('Monitor LG 27"', 'Electrónica', 35, 950000.00, 6),
('Teclado Mecánico Redragon', 'Periféricos', 80, 220000.00, 7),
('Mouse Logitech G Pro', 'Periféricos', 60, 180000.00, 8),
('Silla Gamer Cougar Armor', 'Muebles', 15, 1250000.00, 9),
('Disco SSD 1TB Samsung', 'Almacenamiento', 40, 380000.00, 9);

INSERT INTO usuarios (username, password, nombre_completo, rol) VALUES
('cgomez', '$2a$10$aoScDQEO.4uKepA6cBbkZugy26XEvT1Pa/fD1aemCyaO0h0QcWf0S', 'Carlos Gómez', 'ADMIN'),
('lperez', '$2a$10$aL2N2DcQAGg8GmrGmYD8mu6m7otjjoXEAAhu7cLFW7TTptpSWUUTG', 'Laura Pérez', 'ENCARGADO'),
('matorres', '$2a$10$W4moBz5pvqhMCiNhuNMCgeFk3xXl4oV5/IvuZJh/laXaGP7eS.zCm', 'Mariana Torres', 'OPERADOR'),
('jhernandez', '$2a$10$sctikZu/tKgmcMkVrbwYe.J4WRrYeVU/auQsW3OQHjcD/04L2VYda', 'Julián Hernández', 'ENCARGADO'),
('arios', '$2a$10$nanvYZL/aLyj.ThPkDwym.nQi1N0zJTPAhhh.esEbUy9gQpYUxgw6', 'Andrés Ríos', 'OPERADOR');

INSERT INTO movimientos_inventario (tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
('ENTRADA', 1, NULL, 6),         
('SALIDA', 2, 7, NULL),          
('TRANSFERENCIA', 5, 6, 8),      
('ENTRADA', 3, NULL, 9),         
('SALIDA', 4, 10, NULL);          


INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad)
VALUES
(31, 1, 20),  -- Entrada de 20 Monitores LG a Bodega Central
(31, 2, 15),
(32, 3, 10),  -- Salida de 10 Mouse Logitech
(33, 4, 5),   -- Transferencia de 5 Sillas Gamer de Central a Sur
(33, 5, 10);

INSERT INTO auditoria (tipo_operacion, usuario_id, entidad_afectada, valor_anterior, valor_nuevo) VALUES
('INSERT', 1, 'Bodega', NULL, '{"id":1,"nombre":"Bodega Central","capacidad":5000}'),
('UPDATE', 2, 'Producto', '{"id":2,"stock":80}', '{"id":2,"stock":75}'),
('DELETE', 5, 'MovimientoInventario', '{"id":3,"tipo":"TRANSFERENCIA"}', NULL),
('INSERT', 3, 'Producto', NULL, '{"id":6,"nombre":"Laptop Dell","precio":3500000}'),
('UPDATE', 4, 'Bodega', '{"id":5,"encargado":"Julián Hernández"}', '{"id":5,"encargado":"Luis Pardo"}');

