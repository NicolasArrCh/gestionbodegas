package com.c3.gestionbodegas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;

@Repository
public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Integer> {

    // Obtener todos los detalles de un movimiento especifico
    List<DetalleMovimiento> findByMovimiento(MovimientoInventario movimientoInventario);

    // Obtener todos los movimientos en los que intervino un producto específico
    List<DetalleMovimiento> findByProducto(Producto producto);

    // Consultar todos los detalles donde un producto tenga cantidad menor a X(util para auditorías o control de stock)
    List<DetalleMovimiento> findByCantidadLessThan(Integer cantidad);

}
