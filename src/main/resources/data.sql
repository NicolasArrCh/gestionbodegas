-- Datos de ejemplo para pruebas

-- ============================================================
-- USUARIOS
-- ============================================================
INSERT INTO usuarios (id, username, password, nombre_completo, rol) VALUES
(1, 'cgomez', '$2a$10$aoScDQEO.4uKepA6cBbkZugy26XEvT1Pa/fD1aemCyaO0h0QcWf0S', 'Carlos Gómez García', 'ADMIN'),
(2, 'lperez', '$2a$10$aL2N2DcQAGg8GmrGmYD8mu6m7otjjoXEAAhu7cLFW7TTptpSWUUTG', 'Laura Pérez Martínez', 'ENCARGADO'),
(3, 'matorres', '$2a$10$W4moBz5pvqhMCiNhuNMCgeFk3xXl4oV5/IvuZJh/laXaGP7eS.zCm', 'Mariana Torres López', 'OPERADOR'),
(4, 'jhernandez', '$2a$10$sctikZu/tKgmcMkVrbwYe.J4WRrYeVU/auQsW3OQHjcD/04L2VYda', 'Julián Hernández Rodríguez', 'ENCARGADO'),
(5, 'arios', '$2a$10$nanvYZL/aLyj.ThPkDwym.nQi1N0zJTPAhhh.esEbUy9gQpYUxgw6', 'Andrés Ríos Jiménez', 'OPERADOR');

-- ============================================================
-- BODEGAS
-- ============================================================
INSERT INTO bodegas (id, nombre, ubicacion, capacidad, encargado_id) VALUES
(1, 'Bodega Central', 'Zona Industrial Norte, Medellín', 5000, 2),
(2, 'Bodega Norte', 'Km 12 Vía Bello, Medellín', 3000, 2),
(3, 'Bodega Sur', 'Parque Logístico del Sur, Itagüí', 4500, 4),
(4, 'Bodega Occidente', 'Av. 80 #45-12, Medellín', 2500, 4),
(5, 'Bodega Oriente', 'Aeropuerto José María Córdova, Rionegro', 6000, 4);

-- ============================================================
-- PRODUCTOS
-- ============================================================
INSERT INTO productos (id, nombre, categoria, stock, precio, bodega_id) VALUES
(1, 'Monitor LG 27"', 'Electrónica', 35, 950000.00, 1),
(2, 'Teclado Mecánico Redragon', 'Periféricos', 80, 220000.00, 1),
(3, 'Mouse Logitech G Pro', 'Periféricos', 60, 180000.00, 2),
(4, 'Silla Gamer Cougar Armor', 'Muebles', 15, 1250000.00, 3),
(5, 'Disco SSD 1TB Samsung', 'Almacenamiento', 40, 380000.00, 4),
(6, 'Laptop Dell XPS 15', 'Computadoras', 12, 3500000.00, 5),
(7, 'Escritorio Gamer Acero', 'Muebles', 22, 850000.00, 1),
(8, 'Cables HDMI Premium', 'Accesorios', 150, 35000.00, 2);

-- ============================================================
-- MOVIMIENTOS DE INVENTARIO
-- ============================================================
INSERT INTO movimientos_inventario (id, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(1, 'ENTRADA', 1, NULL, 1),
(2, 'SALIDA', 2, 2, NULL),
(3, 'TRANSFERENCIA', 3, 1, 3),
(4, 'ENTRADA', 1, NULL, 4),
(5, 'SALIDA', 5, 5, NULL),
(6, 'TRANSFERENCIA', 2, 2, 1);

-- ============================================================
-- DETALLES DE MOVIMIENTOS
-- ============================================================
INSERT INTO detalle_movimiento (id, movimiento_id, producto_id, cantidad)
VALUES
(1, 1, 1, 20),
(2, 1, 2, 15),
(3, 2, 3, 10),
(4, 3, 4, 5),
(5, 3, 5, 10),
(6, 4, 6, 8),
(7, 5, 7, 12),
(8, 6, 8, 50);

-- ============================================================
-- AUDITORÍA
-- ============================================================
INSERT INTO auditoria (id, tipo_operacion, usuario_id, entidad_afectada, valor_anterior, valor_nuevo) VALUES
(1, 'INSERT', 1, 'Bodega', NULL, '{"id":1,"nombre":"Bodega Central","capacidad":5000}'),
(2, 'UPDATE', 2, 'Producto', '{"id":1,"stock":15}', '{"id":1,"stock":35}'),
(3, 'INSERT', 1, 'Producto', NULL, '{"id":6,"nombre":"Laptop Dell XPS 15","precio":3500000}'),
(4, 'UPDATE', 3, 'MovimientoInventario', '{"id":3,"tipo":"SALIDA"}', '{"id":3,"tipo":"TRANSFERENCIA"}'),
(5, 'INSERT', 5, 'MovimientoInventario', NULL, '{"id":6,"tipo":"TRANSFERENCIA"}')

-- Actualizar usuarios existentes
UPDATE usuarios 
SET fecha_creacion = NOW(), 
    fecha_modificacion = NOW() 
WHERE fecha_creacion IS NULL;

-- Actualizar bodegas existentes
UPDATE bodegas 
SET fecha_creacion = NOW(), 
    fecha_modificacion = NOW() 
WHERE fecha_creacion IS NULL;

-- Actualizar productos existentes
UPDATE productos 
SET fecha_creacion = NOW(), 
    fecha_modificacion = NOW() 
WHERE fecha_creacion IS NULL;

-- Actualizar movimientos existentes
UPDATE movimientos_inventario 
SET fecha_creacion = NOW(), 
    fecha_modificacion = NOW() 
WHERE fecha_creacion IS NULL;

-- Actualizar detalles existentes
UPDATE detalle_movimiento 
SET fecha_creacion = NOW(), 
    fecha_modificacion = NOW() 
WHERE fecha_creacion IS NULL;