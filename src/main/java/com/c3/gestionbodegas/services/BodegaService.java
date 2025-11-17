package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.repository.BodegaRepository;

@Service
public class BodegaService {

    @Autowired
    private BodegaRepository bodegaRepository;

    // Obtener todas las bodegas
    public List<Bodega> obtenerTodas() {
        return bodegaRepository.findAll();
    }

    // Buscar una bodega por su ID
    public Optional<Bodega> buscarPorId(Integer id) {
        return bodegaRepository.findById(id);
    }

    // Guardar o actualizar una bodega
    public Bodega guardar(Bodega bodega) {
        return bodegaRepository.save(bodega);
    }

    // Eliminar una bodega por su ID
    public void eliminar(Integer id) {
        bodegaRepository.deleteById(id);
    }

    // Buscar una bodega por su nombre exacto
    public Bodega buscarPorNombre(String nombre) {
        return bodegaRepository.findByNombre(nombre);
    }

    // Verificar si ya existe una bodega con ese nombre
    public boolean existePorNombre(String nombre) {
        return bodegaRepository.existsByNombre(nombre);
    }

    // Buscar bodegas por ubicación (ignorando mayúsculas/minúsculas)
    public List<Bodega> buscarPorUbicacion(String ubicacion) {
        return bodegaRepository.findByUbicacionContainingIgnoreCase(ubicacion);
    }

    // Buscar bodegas con capacidad menor a cierto valor
    public List<Bodega> buscarPorCapacidadMenorA(Integer capacidad) {
        return bodegaRepository.findByCapacidadLessThan(capacidad);
    }

    // Obtener resumen de stock total por bodega (devuelve lista de Object[])
    public List<Object[]> obtenerResumenStockPorBodega() {
        return bodegaRepository.obtenerResumenStockPorBodega();
    }
}
