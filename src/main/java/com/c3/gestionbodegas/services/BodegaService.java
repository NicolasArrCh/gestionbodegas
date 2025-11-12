package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.repository.BodegaRepository;

@Service
public class BodegaService {

    @Autowired
    private final BodegaRepository bodegaRepository;

    public List<Bodega> obtenerTodoBodega() {
        return bodegaRepository.findAll();
    }

    public Bodega buscarBodegaPorId(Long id) {
        return bodegaRepository.findById(id).orElse(null);
    }

    public List<Bodega> buscarPorNombre(String nombre) {
        return bodegaRepository.findByNombre(nombre);
    }

    public Bodega guardarBodega(Bodega bodega) {
        return bodegaRepository.save(bodega); // insert into .... values ...
    }

    public void eliminarBodega(Long id) {
        bodegaRepository.deleteById(id);
    }

    public boolean actualizarBodega(Long id, Bodega bodegaActualizada) {
        int filasActualizadas = bodegaRepository.actualizarBodega (
            id,
            bodegaActualizada.getNombre());
        
        return filasActualizadas > 0;
    }
}
