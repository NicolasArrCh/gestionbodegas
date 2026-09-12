package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/intentos-fallidos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class IntentoFallidoController {

    private final IntentoFallidoService intentoFallidoService;

    /**
     * Obtener todos los intentos fallidos
     */
    @GetMapping
    public ResponseEntity<List<IntentoFallido>> obtenerTodos() {
        return ResponseEntity.ok(intentoFallidoService.obtenerTodos());
    }

    /**
     * Obtener todos los intentos fallidos paginados
     */
    @GetMapping("/paginado")
    public ResponseEntity<Page<IntentoFallido>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(intentoFallidoService.obtenerTodosPaginado(pageable));
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
     */
    @GetMapping("/por-tipo")
    public ResponseEntity<List<IntentoFallido>> obtenerPorTipo(
            @RequestParam IntentoFallido.TipoMovimiento tipo) {
        return ResponseEntity.ok(intentoFallidoService.obtenerPorTipo(tipo));
    }

    /**
     * Buscar intentos fallidos por razón de error
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<IntentoFallido>> buscarPorError(
            @RequestParam String error) {
        return ResponseEntity.ok(intentoFallidoService.buscarPorError(error));
    }

    /**
     * Obtener intentos fallidos en rango de fechas
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
