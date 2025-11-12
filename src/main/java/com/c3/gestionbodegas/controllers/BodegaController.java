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

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.services.BodegaService;

@RestController
@RequestMapping("/bodega")
public class BodegaController {

    @Autowired
    private final BodegaService bodegaService;

    // Devolver todas las bodegas
    @GetMapping
    public ResponseEntity<List<Bodega>> obtenerTodoBodega() {
        List<Bodega> bodega = auditoriaService.obtenerTodoBodega();
        return bodega.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(bodega);
    }

    // Buscar por el nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Bodega>> buscarPorNombre(@RequestParam String nombre) {
        List<Bodega> bodega = bodegaService.buscarPorNombre(nombre);
        return bodega.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(bodega);
    }

    // Buscar por ID
     @GetMapping("/{id}")
    public ResponseEntity<List<Bodega>> buscarPorId(@PathVariable Long id) {
        Bodega bodega = bodegaService.buscarPorId(id);
        return bodega != null ? ResponseEntity.ok(bodega) : ResponseEntity.ok(bodega);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Bodega> guardarBodega(@RequestBody Bodega bodega) {
        Bodega bodegaNuevo = bodegaService.guardarBodega(bodega);

        return ResponseEntity.ok(BodegaNuevo);
    }

    // Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBodega(@PathVariable Long id) {
        if (bodegaService.obtenerTodoBodega().stream().noneMatch(a -> a.getId().equals(id))) {
            return ResponseEntity.notFound().build();
        }

        bodegaService.eliminarBodega(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoind del patch
    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizarBodega(
        @PathVariable Long id,
        @RequestBody Bodega bodegaActualizada) {

            boolean actualizado = bodegaService.actualizarBodega(id, bodegaActualizada);
            return actualizado ?
                ResponseEntity.ok("Bodega actualizada con éxito.") :
                ResponseEntity.notFound().build();
        }
}
