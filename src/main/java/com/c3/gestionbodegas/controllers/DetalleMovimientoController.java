package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ✅ Obtener todos los detalles de movimiento
    @GetMapping
    public ResponseEntity<List<DetalleMovimiento>> obtenerTodos() {
        List<DetalleMovimiento> detalles = detalleMovimientoService.obtenerTodos();
        return ResponseEntity.ok(detalles);
    }

    // ✅ Obtener un detalle de movimiento por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetalleMovimiento> obtenerPorId(@PathVariable Integer id) {
        DetalleMovimiento detalle = detalleMovimientoService.obtenerPorId(id);
        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalle);
    }

    // ✅ Crear un nuevo detalle de movimiento
    @PostMapping
    public ResponseEntity<DetalleMovimiento> crear(@RequestBody DetalleMovimiento detalleMovimiento) {
        DetalleMovimiento nuevo = detalleMovimientoService.guardar(detalleMovimiento);
        return ResponseEntity.ok(nuevo);
    }

    // ✅ Actualizar un detalle existente
    @PutMapping("/{id}")
    public ResponseEntity<DetalleMovimiento> actualizar(@PathVariable Integer id, @RequestBody DetalleMovimiento detalleMovimiento) {
        DetalleMovimiento actualizado = detalleMovimientoService.actualizar(id, detalleMovimiento);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    // ✅ Eliminar un detalle de movimiento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = detalleMovimientoService.eliminar(id);
        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ✅ Obtener todos los detalles de un movimiento específico
    @GetMapping("/movimiento/{idMovimiento}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorMovimiento(@PathVariable Integer idMovimiento) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setId(idMovimiento);
        List<DetalleMovimiento> detalles = detalleMovimientoService.buscarPorMovimiento(movimiento);
        return ResponseEntity.ok(detalles);
    }

    // ✅ Obtener todos los movimientos en los que intervino un producto específico
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorProducto(@PathVariable Integer idProducto) {
        Producto producto = new Producto();
        producto.setId(idProducto);
        List<DetalleMovimiento> detalles = detalleMovimientoService.buscarPorProducto(producto);
        return ResponseEntity.ok(detalles);
    }

    // ✅ Consultar todos los detalles donde un producto tenga cantidad menor a X
    @GetMapping("/cantidad-menor/{cantidad}")
    public ResponseEntity<List<DetalleMovimiento>> obtenerPorCantidadMenorA(@PathVariable Integer cantidad) {
        List<DetalleMovimiento> detalles = detalleMovimientoService.buscarPorCantidadMenorA(cantidad);
        return ResponseEntity.ok(detalles);
    }
}
