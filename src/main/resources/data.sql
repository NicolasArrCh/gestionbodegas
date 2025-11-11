INSERT INTO bodegas (nombre, ubicacion, capacidad, encargado) VALUES
('Bodega Central', 'Zona Industrial Norte, Medellín', 5000, 'Carlos Gómez'),
('Bodega Norte', 'Km 12 Vía Bello, Medellín', 3000, 'Laura Pérez'),
('Bodega Sur', 'Parque Logístico del Sur, Itagüí', 4500, 'Andrés Ríos'),
('Bodega Occidente', 'Av. 80 #45-12, Medellín', 2500, 'Mariana Torres'),
('Bodega Oriente', 'Aeropuerto José María Córdova, Rionegro', 6000, 'Julián Hernández');

INSERT INTO productos (nombre, categoria, stock, precio) VALUES
('Monitor LG 27"', 'Electrónica', 35, 950000.00),
('Teclado Mecánico Redragon', 'Periféricos', 80, 220000.00),
('Mouse Logitech G Pro', 'Periféricos', 60, 180000.00),
('Silla Gamer Cougar Armor', 'Muebles', 15, 1250000.00),
('Disco SSD 1TB Samsung', 'Almacenamiento', 40, 380000.00);

INSERT INTO movimientos_inventario (tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
('ENTRADA', 1, NULL, 1),         -- Carlos Gómez ingresa productos a Bodega Central
('SALIDA', 2, 2, NULL),          -- Laura Pérez retira productos de Bodega Norte
('TRANSFERENCIA', 5, 1, 3),      -- Andrés Ríos transfiere de Central a Sur
('ENTRADA', 3, NULL, 4),         -- Mariana Torres ingresa a Bodega Occidente
('SALIDA', 4, 5, NULL);          -- Julián Hernández saca de Bodega Oriente


INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad)
VALUES
(1, 1, 20),  -- Entrada de 20 Monitores LG a Bodega Central
(1, 2, 15),
(2, 3, 10),  -- Salida de 10 Mouse Logitech
(3, 4, 5),   -- Transferencia de 5 Sillas Gamer de Central a Sur
(3, 5, 10);

INSERT INTO auditoria (tipo_operacion, usuario_id, entidad_afectada, valor_anterior, valor_nuevo) VALUES
('INSERT', 1, 'Bodega', NULL, '{"id":1,"nombre":"Bodega Central","capacidad":5000}'),
('UPDATE', 2, 'Producto', '{"id":2,"stock":80}', '{"id":2,"stock":75}'),
('DELETE', 5, 'MovimientoInventario', '{"id":3,"tipo":"TRANSFERENCIA"}', NULL),
('INSERT', 3, 'Producto', NULL, '{"id":6,"nombre":"Laptop Dell","precio":3500000}'),
('UPDATE', 4, 'Bodega', '{"id":5,"encargado":"Julián Hernández"}', '{"id":5,"encargado":"Luis Pardo"}');

INSERT INTO usuarios (username, password, nombre_completo, rol) VALUES
('cgomez', '1234', 'Carlos Gómez', 'ADMIN'),
('lperez', '1234', 'Laura Pérez', 'ENCARGADO'),
('matorres', '1234', 'Mariana Torres', 'OPERADOR'),
('jhernandez', '1234', 'Julián Hernández', 'ENCARGADO'),
('arios', '1234', 'Andrés Ríos', 'OPERADOR');