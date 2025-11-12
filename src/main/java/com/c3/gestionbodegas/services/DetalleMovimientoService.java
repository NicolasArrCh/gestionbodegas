package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.repository.DetalleMovimientoRepository;

@Service
public class DetalleMovimientoService {

    @Autowired
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public List<DetalleMovimiento> obtenerTodoDetalleMovimiento() {
        return detalleMovimientoRepository.findAll();
    }

    public DetalleMovimiento buscarDetalleMovimientoPorId(Long id) {
        return detalleMovimientoRepository.findById(id).orElse(null);
    }

    public List<DetalleMovimiento> buscarPorNombre(String nombre) {
        return detalleMovimientoRepository.findByNombre(nombre);
    }

    public DetalleMovimiento guardarDetalleMovimiento(DetalleMovimiento detalleMovimiento) {
        return detalleMovimientoRepository.save(detalleMovimiento); // insert into .... values ...
    }

    public void eliminarDetalleMovimiento(Long id) {
        detalleMovimientoRepository.deleteById(id);
    }

    public boolean actualizarDetalleMovimiento(Long id, DetalleMovimiento detalleMovimientoActualizada) {
        int filasActualizadas = detalleMovimientoRepository.actualizarDetalleMovimiento (
            id,
            detalleMovimientoActualizada.getNombre());
        
        return filasActualizadas > 0;
    }
}
