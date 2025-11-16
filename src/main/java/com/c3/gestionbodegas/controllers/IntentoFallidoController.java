package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.services.IntentoFallidoService;

@RestController
@RequestMapping("/api/intentos-fallidos")
@CrossOrigin(origins = "*")
public class IntentoFallidoController {

    @Autowired
    private IntentoFallidoService intentoFallidoService;

    /**
     * Obtener todos los intentos fallidos
     */
    @GetMapping
    public ResponseEntity<List<IntentoFallido>> obtenerTodos() {
        return ResponseEntity.ok(intentoFallidoService.obtenerTodos());
    }

    /**
     * Obtener los últimos 50 intentos fallidos
     */
    @GetMapping("/ultimos")
    public ResponseEntity<List<IntentoFallido>> obtenerUltimos() {
        return ResponseEntity.ok(intentoFallidoService.obtenerUltimos());
    }

    /**
     * Obtener un intento fallido por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<IntentoFallido> obtenerPorId(@PathVariable Long id) {
        IntentoFallido intento = intentoFallidoService.obtenerPorId(id);
        if (intento == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(intento);
    }

    /**
     * Obtener intentos fallidos por tipo de movimiento
     * /api/intentos-fallidos/por-tipo?tipo=SALIDA
     */
    @GetMapping("/por-tipo")
    public ResponseEntity<List<IntentoFallido>> obtenerPorTipo(
            @RequestParam IntentoFallido.TipoMovimiento tipo) {
        return ResponseEntity.ok(intentoFallidoService.obtenerPorTipo(tipo));
    }

    /**
     * Buscar intentos fallidos por razón de error
     * /api/intentos-fallidos/buscar?error=stock
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<IntentoFallido>> buscarPorError(
            @RequestParam String error) {
        return ResponseEntity.ok(intentoFallidoService.buscarPorError(error));
    }

    /**
     * Obtener intentos fallidos en rango de fechas
     * /api/intentos-fallidos/por-fecha?inicio=2025-01-01T00:00:00&fin=2025-12-31T23:59:59
     */
    @GetMapping("/por-fecha")
    public ResponseEntity<List<IntentoFallido>> obtenerPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(intentoFallidoService.obtenerPorFechas(inicio, fin));
    }

    /**
     * Eliminar un intento fallido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (intentoFallidoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
