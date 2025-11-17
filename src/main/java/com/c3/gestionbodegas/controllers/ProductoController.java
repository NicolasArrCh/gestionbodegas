package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.services.ProductoService;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ✅ Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    // ✅ Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    // ✅ Crear un nuevo producto
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto nuevo = productoService.guardar(producto);
        return ResponseEntity.ok(nuevo);
    }

    // ✅ Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Integer id, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizar(id, producto);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    // ✅ Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = productoService.eliminar(id);
        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ✅ Buscar producto por nombre exacto
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Producto> buscarPorNombre(@PathVariable String nombre) {
        Producto producto = productoService.buscarPorNombre(nombre);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    // ✅ Buscar productos por categoría (contiene, sin importar mayúsculas)
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable String categoria) {
        List<Producto> productos = productoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    // ✅ Buscar productos con stock menor a X (para alertas)
    @GetMapping("/stock-bajo/{cantidad}")
    public ResponseEntity<List<Producto>> buscarPorStockMenorA(@PathVariable Integer cantidad) {
        List<Producto> productos = productoService.buscarPorStockBajo(cantidad);
        return ResponseEntity.ok(productos);
    }

    // ✅ Verificar si existe un producto por nombre
    @GetMapping("/existe/{nombre}")
    public ResponseEntity<Boolean> existePorNombre(@PathVariable String nombre) {
        boolean existe = productoService.existePorNombre(nombre);
        return ResponseEntity.ok(existe);
    }

    // ✅ Obtener los productos más movidos (consulta personalizada)
    @GetMapping("/mas-movidos")
    public ResponseEntity<List<Object[]>> obtenerProductosMasMovidos() {
        List<Object[]> productos = productoService.obtenerProductosMasMovidos();
        return ResponseEntity.ok(productos);
    }
}
