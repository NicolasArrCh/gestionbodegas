package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Auditoria.TipoOperacion;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.AuditoriaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auditorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // ✅ Obtener todas las auditorías (completa para compatibilidad)
    @GetMapping
    public ResponseEntity<List<Auditoria>> obtenerTodas() {
        List<Auditoria> auditorias = auditoriaService.obtenerTodas();
        return ResponseEntity.ok(auditorias);
    }

    // ✅ Obtener auditorías paginadas
    @GetMapping("/paginado")
    public ResponseEntity<Page<Auditoria>> obtenerTodasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(auditoriaService.obtenerTodasPaginado(pageable));
    }

    // ✅ Guardar una nueva auditoría
    @PostMapping
    public ResponseEntity<Auditoria> guardar(@RequestBody Auditoria auditoria) {
        Auditoria nueva = auditoriaService.guardar(auditoria);
        return ResponseEntity.ok(nueva);
    }

    // ✅ Buscar auditorías por tipo de operación
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Auditoria>> buscarPorTipo(@PathVariable("tipo") TipoOperacion tipoOperacion) {
        List<Auditoria> auditorias = auditoriaService.buscarPorTipoOperacion(tipoOperacion);
        return ResponseEntity.ok(auditorias);
    }

    // ✅ Buscar auditorías por entidad afectada
    @GetMapping("/entidad/{entidad}")
    public ResponseEntity<List<Auditoria>> buscarPorEntidad(@PathVariable("entidad") String entidad) {
        List<Auditoria> auditorias = auditoriaService.buscarPorEntidadAfectada(entidad);
        return ResponseEntity.ok(auditorias);
    }

    // ✅ Buscar auditorías por rango de fechas
    @GetMapping("/fechas")
    public ResponseEntity<List<Auditoria>> buscarPorFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<Auditoria> auditorias = auditoriaService.buscarPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(auditorias);
    }

    // ✅ Buscar auditorías por nombre de usuario y tipo de operación
    @GetMapping("/usuario/{username}/tipo/{tipo}")
    public ResponseEntity<List<Auditoria>> buscarPorUsuarioYTipo(
            @PathVariable("username") String username,
            @PathVariable("tipo") TipoOperacion tipoOperacion) {

        List<Auditoria> auditorias = auditoriaService.buscarPorUsuarioYTipo(username, tipoOperacion);
        return ResponseEntity.ok(auditorias);
    }

    // ✅ Buscar auditorías por usuario (requiere objeto Usuario)
    @PostMapping("/usuario")
    public ResponseEntity<List<Auditoria>> buscarPorUsuario(@RequestBody Usuario usuario) {
        List<Auditoria> auditorias = auditoriaService.buscarPorUsuario(usuario);
        return ResponseEntity.ok(auditorias);
    }
}
