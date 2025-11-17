package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.services.DetalleMovimientoService;

@RestController
@RequestMapping("/api/detalle-movimientos")
@CrossOrigin(origins = "*")
public class DetalleMovimientoController {

    @Autowired
    private DetalleMovimientoService detalleMovimientoService;

    // Obtener todos los detalles
    @GetMapping
    public ResponseEntity<List<DetalleMovimiento>> obtenerTodos() {
        return ResponseEntity.ok(detalleMovimientoService.obtenerTodos());
    }

    // Obtener detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetalleMovimiento> obtenerPorId(@PathVariable Integer id) {
        DetalleMovimiento detalle = detalleMovimientoService.obtenerPorId(id);

        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detalle);
    }

    // Crear un detalle
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody DetalleMovimiento detalleMovimiento) {
        try {
            DetalleMovimiento nuevo = detalleMovimientoService.guardar(detalleMovimiento);
            return ResponseEntity.ok(nuevo);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Actualizar un detalle existente
    @PutMapping("/{id}")
    public ResponseEntity<DetalleMovimiento> actualizar(
            @PathVariable Integer id,
            @RequestBody DetalleMovimiento detalleMovimiento) {

        DetalleMovimiento actualizado = detalleMovimientoService.actualizar(id, detalleMovimiento);

        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un detalle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = detalleMovimientoService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // Buscar por movimiento
    @GetMapping("/movimiento/{idMovimiento}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorMovimiento(@PathVariable Integer idMovimiento) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setId(idMovimiento);

        return ResponseEntity.ok(
            detalleMovimientoService.buscarPorMovimiento(movimiento)
        );
    }

    // Buscar por producto
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorProducto(@PathVariable Integer idProducto) {
        Producto producto = new Producto();
        producto.setId(idProducto);

        return ResponseEntity.ok(
            detalleMovimientoService.buscarPorProducto(producto)
        );
    }

    // Buscar por cantidad menor a X
    @GetMapping("/cantidad-menor/{cantidad}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorCantidadMenorA(@PathVariable Integer cantidad) {
        return ResponseEntity.ok(
            detalleMovimientoService.buscarPorCantidadMenorA(cantidad)
        );
    }
}