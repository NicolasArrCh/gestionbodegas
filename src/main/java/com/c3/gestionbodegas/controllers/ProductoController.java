package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.services.ProductoService;

@RestController
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private final ProductoService productoService;

    // Devolver todas las auditorias
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodoProducto() {
        List<Producto> producto = productoService.obtenerTodoProducto();
        return producto.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(producto);
    }

    // Buscar por el nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> producto = productoService.buscarPorNombre(nombre);
        return producto.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(producto);
    }

    // Buscar por ID
     @GetMapping("/{id}")
    public ResponseEntity<List<Producto>> buscarPorId(@PathVariable Long id) {
        Producto producto = productoService.buscarPorId(id);
        return producto != null ? ResponseEntity.ok(producto) : ResponseEntity.ok(producto);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Producto> guardarProducto(@RequestBody Producto producto) {
        Producto productoNuevo = productoService.guardarProducto(producto);

        return ResponseEntity.ok(productoNuevo);
    }

    // Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (productoService.obtenerTodoProducto().stream().noneMatch(a -> a.getId().equals(id))) {
            return ResponseEntity.notFound().build();
        }

        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoind del patch
    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizarProducto(
        @PathVariable Long id,
        @RequestBody Producto productoActualizado) {

            boolean actualizado = productoService.actualizarProducto(id, productoActualizado);
            return actualizado ?
                ResponseEntity.ok("Producto actualizado con éxito.") :
                ResponseEntity.notFound().build();
        }
}
