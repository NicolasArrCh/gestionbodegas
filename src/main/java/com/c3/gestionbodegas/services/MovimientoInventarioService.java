package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.repository.MovimientoInventarioRepository;

@Service
public class MovimientoInventarioService {

    @Autowired
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public List<MovimientoInventario> obtenerTodoMovimientoInventario() {
        return movimientoInventarioRepository.findAll();
    }

    public MovimientoInventario buscarMovimientoInventarioPorId(Long id) {
        return movimientoInventarioRepository.findById(id).orElse(null);
    }

    public List<MovimientoInventario> buscarPorNombre(String nombre) {
        return movimientoInventarioRepository.findByNombre(nombre);
    }

    public MovimientoInventario guardarMovimientoInventario(MovimientoInventario movimientoInventario) {
        return movimientoInventarioRepository.save(movimientoInventario); // insert into .... values ...
    }

    public void eliminarMovimientoInventario(Long id) {
        movimientoInventarioRepository.deleteById(id);
    }

    public boolean actualizarMovimientoInventario(Long id, MovimientoInventario movimientoInventarioActualizada) {
        int filasActualizadas = movimientoInventarioRepository.actualizarMovimientoInventario (
            id,
            movimientoInventarioActualizada.getNombre());
        
        return filasActualizadas > 0;
    }
}
