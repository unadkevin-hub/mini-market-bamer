-- ============================================================
-- Mini Market Bamer - Script de base de datos (MySQL 8)
-- Evidencia GA7-220501096-AA2-EV01
-- Aprendiz: Kevin Andres Oliveros Canchila - Ficha 3336091
-- ============================================================

CREATE DATABASE IF NOT EXISTS bamer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bamer;

DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS categorias;

CREATE TABLE categorias (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras       VARCHAR(14)   NOT NULL UNIQUE,
    nombre              VARCHAR(60)   NOT NULL,
    id_categoria        INT           NOT NULL,
    descripcion         VARCHAR(200),
    precio_compra       DECIMAL(12,2) NOT NULL CHECK (precio_compra >= 0),
    precio_venta        DECIMAL(12,2) NOT NULL,
    stock               INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo        INT           NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0),
    fecha_creacion      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_productos_precios CHECK (precio_venta >= precio_compra),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias (id)
);

INSERT INTO categorias (nombre) VALUES
    ('Granos'), ('Lacteos'), ('Bebidas'), ('Aseo'), ('Abarrotes');

INSERT INTO productos (codigo_barras, nombre, id_categoria, descripcion, precio_compra, precio_venta, stock, stock_minimo) VALUES
    ('7701234500011', 'Arroz Diana 500 g',      1, 'Arroz blanco premium',        2100, 2600, 40, 10),
    ('7701234500028', 'Frijol cargamanto 500 g',1, 'Frijol rojo seleccionado',    4800, 5900, 22, 8),
    ('7701234500035', 'Leche Colanta 1 L',      2, 'Leche entera larga vida',     3300, 3900, 30, 12),
    ('7701234500042', 'Huevos AA x 12',         5, 'Cubeta de 12 huevos',         9500, 11500, 4, 6),
    ('7701234500059', 'Cafe Sello Rojo 250 g',  3, 'Cafe tostado y molido',       6800, 8200, 15, 6),
    ('7701234500066', 'Azucar Manuelita 1 kg',  5, 'Azucar blanca refinada',      3600, 4300, 3, 8);
