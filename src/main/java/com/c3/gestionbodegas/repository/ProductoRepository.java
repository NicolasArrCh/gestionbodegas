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

    // Buscar productos con stock mayor a X (útil para listar productos disponibles)
    List<Producto> findByStockGreaterThan(Integer cantidad);

    // Verificar si ya existe un producto con ese nombre
    boolean existsByNombre(String nombre);

    // Buscar producto por nombre en una bodega específica
    Producto findByNombreAndBodega(String nombre, com.c3.gestionbodegas.entities.Bodega bodega);

    // Obtener lista de productos por bodega
    List<Producto> findByBodega(com.c3.gestionbodegas.entities.Bodega bodega);

    // Calcular stock total de una bodega (suma de stock de todos sus productos)
    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Producto p WHERE p.bodega.id = :bodegaId")
    Integer obtenerStockTotalPorBodega(Integer bodegaId);

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
