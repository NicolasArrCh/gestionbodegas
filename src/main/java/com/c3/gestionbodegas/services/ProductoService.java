package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private final ProductoRepository productoRepository;

    public List<Producto> obtenerTodoProducto() {
        return productoRepository.findAll();
    }

    public Producto buscarProductoPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombre(nombre);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto); // insert into .... values ...
    }

    public boolean eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public boolean actualizarProducto(Long id, Producto productoActualizado) {
        int filasActualizadas = productoRepository.actualizarProducto (
            id,
            productoActualizado.getNombre());
        
        return filasActualizadas > 0;
    }
}
