package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.repository.DetalleMovimientoRepository;

@Service
public class DetalleMovimientoService {

    @Autowired
    private DetalleMovimientoRepository detalleMovimientoRepository;

    // Obtener todos los detalles de movimiento
    public List<DetalleMovimiento> obtenerTodos() {
        return detalleMovimientoRepository.findAll();
    }

    // Buscar un detalle específico por su ID
    public Optional<DetalleMovimiento> buscarPorId(Integer id) {
        return detalleMovimientoRepository.findById(id);
    }

    // Guardar o actualizar un detalle de movimiento
    public DetalleMovimiento guardar(DetalleMovimiento detalleMovimiento) {
        return detalleMovimientoRepository.save(detalleMovimiento);
    }

    // Eliminar un detalle por su ID
    public void eliminar(Integer id) {
        detalleMovimientoRepository.deleteById(id);
    }

    // Obtener todos los detalles de un movimiento específico
    public List<DetalleMovimiento> buscarPorMovimiento(MovimientoInventario movimientoInventario) {
        return detalleMovimientoRepository.findByMovimiento(movimientoInventario);
    }

    // Obtener todos los movimientos en los que intervino un producto específico
    public List<DetalleMovimiento> buscarPorProducto(Producto producto) {
        return detalleMovimientoRepository.findByProducto(producto);
    }

    // Consultar detalles donde la cantidad es menor a un valor (útil para auditorías o alertas)
    public List<DetalleMovimiento> buscarPorCantidadMenorA(Integer cantidad) {
        return detalleMovimientoRepository.findByCantidadLessThan(cantidad);
    }
}
