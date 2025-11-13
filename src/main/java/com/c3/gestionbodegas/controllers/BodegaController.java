package com.c3.gestionbodegas.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.services.BodegaService;

@RestController
@RequestMapping("/api/bodegas")
@CrossOrigin(origins = "*") // permite llamadas desde cualquier origen (React, Angular, etc.)
public class BodegaController {

    @Autowired
    private BodegaService bodegaService;

    // ✅ Obtener todas las bodegas
    @GetMapping
    public ResponseEntity<List<Bodega>> obtenerTodas() {
        List<Bodega> bodegas = bodegaService.obtenerTodas();
        return ResponseEntity.ok(bodegas);
    }

    // ✅ Buscar una bodega por ID
    @GetMapping("/{id}")
    public ResponseEntity<Bodega> buscarPorId(@PathVariable Integer id) {
        Optional<Bodega> bodegaOpt = bodegaService.buscarPorId(id);
        return bodegaOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Crear una nueva bodega
    @PostMapping
    public ResponseEntity<Bodega> crear(@RequestBody Bodega bodega) {
        // Verificar si ya existe una bodega con ese nombre
        if (bodegaService.existePorNombre(bodega.getNombre())) {
            return ResponseEntity.badRequest().build();
        }
        Bodega nueva = bodegaService.guardar(bodega);
        return ResponseEntity.ok(nueva);
    }

    // ✅ Actualizar una bodega existente
    @PutMapping("/{id}")
    public ResponseEntity<Bodega> actualizar(@PathVariable Integer id, @RequestBody Bodega bodega) {
        Optional<Bodega> existente = bodegaService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Actualiza los campos
        Bodega bodegaActualizada = existente.get();
        bodegaActualizada.setNombre(bodega.getNombre());
        bodegaActualizada.setUbicacion(bodega.getUbicacion());
        bodegaActualizada.setCapacidad(bodega.getCapacidad());
        bodegaActualizada.setEncargado(bodega.getEncargado());

        Bodega actualizada = bodegaService.guardar(bodegaActualizada);
        return ResponseEntity.ok(actualizada);
    }

    // ✅ Eliminar una bodega por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Optional<Bodega> bodega = bodegaService.buscarPorId(id);
        if (bodega.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        bodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Buscar bodegas por nombre exacto
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Bodega> buscarPorNombre(@PathVariable String nombre) {
        Bodega bodega = bodegaService.buscarPorNombre(nombre);
        if (bodega == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bodega);
    }

    // ✅ Buscar bodegas por ubicación (coincidencia parcial)
    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<List<Bodega>> buscarPorUbicacion(@PathVariable String ubicacion) {
        List<Bodega> bodegas = bodegaService.buscarPorUbicacion(ubicacion);
        return ResponseEntity.ok(bodegas);
    }

    // ✅ Buscar bodegas con capacidad menor a cierto valor
    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<Bodega>> buscarPorCapacidadMenorA(@PathVariable Integer capacidad) {
        List<Bodega> bodegas = bodegaService.buscarPorCapacidadMenorA(capacidad);
        return ResponseEntity.ok(bodegas);
    }

    // ✅ Obtener resumen de stock total por bodega
    @GetMapping("/resumen-stock")
    public ResponseEntity<List<Object[]>> obtenerResumenStock() {
        List<Object[]> resumen = bodegaService.obtenerResumenStockPorBodega();
        return ResponseEntity.ok(resumen);
    }
}
