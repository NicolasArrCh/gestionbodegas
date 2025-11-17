package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.exception.CapacidadExcedidaException;
import com.c3.gestionbodegas.repository.BodegaRepository;
import com.c3.gestionbodegas.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private BodegaRepository bodegaRepository;

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
        // Validar capacidad de la bodega antes de guardar
        validarCapacidadBodega(producto, null);
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
            // Validar capacidad si cambia el stock o la bodega
            validarCapacidadBodega(producto, id);
            
            p.setNombre(producto.getNombre());
            p.setCategoria(producto.getCategoria());
            p.setStock(producto.getStock());
            p.setPrecio(producto.getPrecio());
            p.setBodega(producto.getBodega());
            return productoRepository.save(p);
        }).orElse(null);
    }

    // Método para validar que el stock no exceda la capacidad de la bodega
    private void validarCapacidadBodega(Producto producto, Integer idProductoExistente) {
        if (producto.getBodega() == null || producto.getBodega().getId() == null) {
            throw new IllegalArgumentException("El producto debe estar asociado a una bodega válida");
        }
        
        Integer bodegaId = producto.getBodega().getId();
        
        // Cargar la bodega completa desde la base de datos
        Bodega bodega = bodegaRepository.findById(bodegaId)
            .orElseThrow(() -> new IllegalArgumentException("La bodega con ID " + bodegaId + " no existe"));
        
        Integer capacidadBodega = bodega.getCapacidad();
        
        // Obtener el stock total actual de la bodega
        Integer stockTotalActual = productoRepository.obtenerStockTotalPorBodega(bodegaId);
        if (stockTotalActual == null) {
            stockTotalActual = 0;
        }
        
        // Si estamos actualizando un producto existente, restar su stock anterior
        if (idProductoExistente != null) {
            Optional<Producto> productoExistente = productoRepository.findById(idProductoExistente);
            if (productoExistente.isPresent()) {
                Integer stockAnterior = productoExistente.get().getStock();
                stockTotalActual -= stockAnterior;
            }
        }
        
        // Calcular el stock total después de agregar/actualizar el producto
        Integer stockTotalNuevo = stockTotalActual + producto.getStock();
        
        // Validar que no se exceda la capacidad
        if (stockTotalNuevo > capacidadBodega) {
            throw new CapacidadExcedidaException(
                String.format("No se puede agregar el producto. La bodega '%s' tiene una capacidad de %d unidades. " +
                             "Stock actual: %d, Stock a agregar: %d, Total resultante: %d",
                             bodega.getNombre(),
                             capacidadBodega,
                             stockTotalActual,
                             producto.getStock(),
                             stockTotalNuevo)
            );
        }
    }
}
