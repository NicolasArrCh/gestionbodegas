package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todos los productos (solo los disponibles con stock > 0)
    public List<Producto> obtenerTodos() {
        return productoRepository.findByStockGreaterThan(0);
    }

    // Buscar un producto por su ID
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Guardar o actualizar un producto
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    // Eliminar un producto por su ID
    public boolean eliminar(Integer id) {
    if (productoRepository.existsById(id)) {
        productoRepository.deleteById(id);
        return true;
    }
    return false;
}

    // Buscar producto por nombre exacto
    public Producto buscarPorNombre(String nombre) {
        return productoRepository.findByNombre(nombre);
    }

    // Buscar productos por categoría (sin importar mayúsculas/minúsculas)
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaContainingIgnoreCase(categoria);
    }

    // Buscar productos con stock bajo (menor que el valor indicado)
    public List<Producto> buscarPorStockBajo(Integer cantidad) {
        return productoRepository.findByStockLessThan(cantidad);
    }

    // Verificar si existe un producto con ese nombre
    public boolean existePorNombre(String nombre) {
        return productoRepository.existsByNombre(nombre);
    }

    // Obtener reporte de los productos más movidos (usando la consulta personalizada)
    public List<Object[]> obtenerProductosMasMovidos() {
        return productoRepository.obtenerProductosMasMovidos();
    }

    public Producto obtenerPorId(Integer id) {
    return productoRepository.findById(id).orElse(null);
}

    @Transactional
    public Producto actualizar(Integer id, Producto producto) {
    return productoRepository.findById(id).map(p -> {
        p.setNombre(producto.getNombre());
        p.setCategoria(producto.getCategoria());
        p.setStock(producto.getStock());
        p.setPrecio(producto.getPrecio());
        p.setBodega(producto.getBodega());
        return productoRepository.save(p);
    }).orElse(null);
}
}
