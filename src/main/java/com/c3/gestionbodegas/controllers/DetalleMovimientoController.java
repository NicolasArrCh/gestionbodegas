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

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.services.DetalleMovimientoService;

@RestController
@RequestMapping("/DetalleMovimiento")
public class DetalleMovimientoController {

    @Autowired
    private DetalleMovimientoService detalleMovimientoService;

    // Devolver todas los detalles de movimiento
    @GetMapping
    public ResponseEntity<DetalleMovimiento> obtenerTodoDetalleMovimiento() {
        List<DetalleMovimiento> detalleMovimiento = detalleMovimientoService.obtenerTodoDetalleMovimiento();
        return detalleMovimiento.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(detalleMovimiento);
    }

    // Buscar por el nombre
    @GetMapping("/buscar")
    public ResponseEntity<DetalleMovimiento> buscarPorNombre(@RequestParam String nombre) {
        List<DetalleMovimiento> detalleMovimiento = detalleMovimientoService.buscarPorNombre(nombre);
        return detalleMovimiento.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(detalleMovimiento);
    }

    // Buscar por ID
     @GetMapping("/{id}")
    public ResponseEntity<DetalleMovimiento> buscarPorId(@PathVariable Long id) {
        DetalleMovimiento detalleMovimiento = detalleMovimientoService.buscarPorId(id);
        return detalleMovimiento != null ? ResponseEntity.ok(detalleMovimiento) : ResponseEntity.ok(detalleMovimiento);
    }

    @PostMapping("/guardar")
    public ResponseEntity<DetalleMovimiento> guardarDetalleMovimiento(@RequestBody DetalleMovimiento detalleMovimiento) {
        DetalleMovimiento detalleMovimientoNuevo = detalleMovimientoService.guardarDetalleMovimiento(detalleMovimiento);

        return ResponseEntity.ok(detalleMovimientoNuevo);
    }

    // Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetalleMovimiento(@PathVariable Long id) {
        boolean eliminado = detalleMovimientoService.eliminarDetalleMovimiento(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Endpoind del patch
    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizarDetalleMovimiento(
        @PathVariable Long id,
        @RequestBody DetalleMovimiento detalleMovimientoActualizada) {

            boolean actualizado = detalleMovimientoService.actualizarDetalleMovimiento(id, detalleMovimientoActualizada);
            return actualizado ?
                ResponseEntity.ok("Detalle y movimiento actualizada con éxito.") :
                ResponseEntity.notFound().build();
        }
}
