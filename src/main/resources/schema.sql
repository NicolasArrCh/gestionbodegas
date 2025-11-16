CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    rol ENUM('ADMIN', 'ENCARGADO', 'OPERADOR') DEFAULT 'OPERADOR'
);

CREATE TABLE bodegas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INT NOT NULL,
    encargado_id INT not null,
    foreign key (encargado_id) references usuarios(id)
);

CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    stock INT NOT NULL,
    precio DECIMAL(10,2) NOT null,
    bodega_id INT NOT NULL,
    FOREIGN KEY (bodega_id) references bodegas(id)
);



CREATE TABLE movimientos_inventario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo ENUM('ENTRADA', 'SALIDA', 'TRANSFERENCIA') NOT NULL,
    usuario_id INT NOT NULL,
    bodega_origen_id INT NULL,
    bodega_destino_id INT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (bodega_origen_id) REFERENCES bodegas(id),
    FOREIGN KEY (bodega_destino_id) REFERENCES bodegas(id)
);


CREATE TABLE detalle_movimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    FOREIGN KEY (movimiento_id) REFERENCES movimientos_inventario(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_operacion ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    valor_anterior JSON NULL,
    valor_nuevo JSON NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ✅ NUEVA TABLA: Intentos Fallidos
CREATE TABLE intentos_fallidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento ENUM('SALIDA', 'ENTRADA', 'TRANSFERENCIA') NOT NULL,
    razon_error VARCHAR(500) NOT NULL,
    usuario_id INT NOT NULL,
    producto_id INT NOT NULL,
    bodega_origen_id INT NULL,
    bodega_destino_id INT NULL,
    cantidad_intentada INT NOT NULL,
    detalles_adicionales JSON NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (bodega_origen_id) REFERENCES bodegas(id),
    FOREIGN KEY (bodega_destino_id) REFERENCES bodegas(id),
    INDEX idx_fecha (fecha_hora),
    INDEX idx_usuario (usuario_id),
    INDEX idx_tipo (tipo_movimiento)
);

-- Tabla usuarios
ALTER TABLE usuarios 
ADD COLUMN fecha_creacion DATETIME,
ADD COLUMN fecha_modificacion DATETIME,
ADD COLUMN creado_por VARCHAR(255),
ADD COLUMN modificado_por VARCHAR(255);

-- Tabla bodegas
ALTER TABLE bodegas 
ADD COLUMN fecha_creacion DATETIME,
ADD COLUMN fecha_modificacion DATETIME,
ADD COLUMN creado_por VARCHAR(255),
ADD COLUMN modificado_por VARCHAR(255);

-- Tabla productos
ALTER TABLE productos 
ADD COLUMN fecha_creacion DATETIME,
ADD COLUMN fecha_modificacion DATETIME,
ADD COLUMN creado_por VARCHAR(255),
ADD COLUMN modificado_por VARCHAR(255);

-- Tabla movimientos_inventario
ALTER TABLE movimientos_inventario 
ADD COLUMN fecha_creacion DATETIME,
ADD COLUMN fecha_modificacion DATETIME,
ADD COLUMN creado_por VARCHAR(255),
ADD COLUMN modificado_por VARCHAR(255);

-- Tabla detalle_movimiento
ALTER TABLE detalle_movimiento 
ADD COLUMN fecha_creacion DATETIME,
ADD COLUMN fecha_modificacion DATETIME,
ADD COLUMN creado_por VARCHAR(255),
ADD COLUMN modificado_por VARCHAR(255);

