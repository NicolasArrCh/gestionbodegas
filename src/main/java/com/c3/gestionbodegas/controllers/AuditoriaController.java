package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.services.AuditoriaService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.c3.gestionbodegas.entities.Auditoria;


@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    // Devolver todas las auditorias
    @GetMapping
    public ResponseEntity<Auditoria> obtenerTodoAuditoria() {
        List<Auditoria> auditoria = auditoriaService.obtenerTodoAuditoria();
        return auditoria.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(auditoria);
    }

    // Buscar por el nombre
    @GetMapping("/buscar")
    public ResponseEntity<Auditoria> buscarPorNombre(@RequestParam String nombre) {
        List<Auditoria> auditoria = auditoriaService.buscarPorNombre(nombre);
        return auditoria.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(auditoria);
    }

    // Buscar por ID
     @GetMapping("/{id}")
    public ResponseEntity<Auditoria> buscarPorId(@PathVariable Long id) {
        Auditoria auditoria = auditoriaService.buscarPorId(id);
        return auditoria != null ? ResponseEntity.ok(auditoria) : ResponseEntity.notFound().build();
    }

    @PostMapping("/guardar")
    public ResponseEntity<Auditoria> guardarAuditoria(@RequestBody Auditoria auditoria) {
        Auditoria auditoriaNueva = auditoriaService.guardarAuditoria(auditoria);

        return ResponseEntity.ok(auditoriaNueva);
    }

    // Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAuditoria(@PathVariable Long id) {
        boolean eliminado = auditoriaService.eliminarAuditoria(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Endpoind del patch
    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizarAuditoria(
        @PathVariable Long id,
        @RequestBody Auditoria auditoriaActualizada) {

            boolean actualizado = auditoriaService.actualizarAuditoria(id, auditoriaActualizada);
            return actualizado ?
                ResponseEntity.ok("Auditoria actualizada con éxito.") :
                ResponseEntity.notFound().build();
        }
}