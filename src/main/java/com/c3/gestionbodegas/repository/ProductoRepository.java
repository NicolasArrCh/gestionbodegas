package com.c3.gestionbodegas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.c3.gestionbodegas.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>{

    // Buscar producto por nombre exacto
    Producto findByNombre(String nombre);

    // Buscar productos por categoría (parcial y sin importar mayúsculas)
    List<Producto> findByCategoriaContainingIgnoreCase(String categoria);

    // Buscar productos con stock bajo (para reportes de alerta)
    List<Producto> findByStockLessThan(Integer cantidad);

    // Verificar si ya existe un producto con ese nombre
    boolean existsByNombre(String nombre);

    // Consulta personalizada para los productos más movidos (puedes usarla en reportes)
    @Query("""
            SELECT p.nombre, SUM(d.cantidad) AS totalMovido
            FROM DetalleMovimiento d
            JOIN Producto p ON d.producto.id = p.id
            GROUP BY p.nombre
            ORDER BY totalMovido DESC
            """)
    List<Object[]> obtenerProductosMasMovidos();

}
