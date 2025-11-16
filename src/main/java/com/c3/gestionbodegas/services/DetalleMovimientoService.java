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

    // Obtener todos
    public List<DetalleMovimiento> obtenerTodos() {
        return detalleMovimientoRepository.findAll();
    }

    // Obtener por ID (ya no tira error)
    public DetalleMovimiento obtenerPorId(Integer id) {
        return detalleMovimientoRepository.findById(id).orElse(null);
    }

    // Guardar
    public DetalleMovimiento guardar(DetalleMovimiento detalleMovimiento) {
        return detalleMovimientoRepository.save(detalleMovimiento);
    }

    // Actualizar
    public DetalleMovimiento actualizar(Integer id, DetalleMovimiento detalleMovimiento) {
        Optional<DetalleMovimiento> existente = detalleMovimientoRepository.findById(id);

        if (existente.isEmpty()) {
            return null;
        }

        DetalleMovimiento detalle = existente.get();

        detalle.setCantidad(detalleMovimiento.getCantidad());
        detalle.setMovimiento(detalleMovimiento.getMovimiento());
        detalle.setProducto(detalleMovimiento.getProducto());

        return detalleMovimientoRepository.save(detalle);
    }

    // Eliminar correctamente
    public boolean eliminar(Integer id) {
        if (!detalleMovimientoRepository.existsById(id)) {
            return false;
        }
        detalleMovimientoRepository.deleteById(id);
        return true;
    }

    // Buscar por movimiento
    public List<DetalleMovimiento> buscarPorMovimiento(MovimientoInventario movimientoInventario) {
        return detalleMovimientoRepository.findByMovimiento(movimientoInventario);
    }

    // Buscar por producto
    public List<DetalleMovimiento> buscarPorProducto(Producto producto) {
        return detalleMovimientoRepository.findByProducto(producto);
    }

    // Buscar por cantidad menor
    public List<DetalleMovimiento> buscarPorCantidadMenorA(Integer cantidad) {
        return detalleMovimientoRepository.findByCantidadLessThan(cantidad);
    }
}