package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.entities.Auditoria.TipoOperacion;
import com.c3.gestionbodegas.services.AuditoriaService;

@RestController
@RequestMapping("/api/auditorias")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (útil para frontends como React o Angular)
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    // ✅ Obtener todas las auditorías
    @GetMapping
    public ResponseEntity<List<Auditoria>> obtenerTodas() {
        List<Auditoria> auditorias = auditoriaService.obtenerTodas();
        return ResponseEntity.ok(auditorias);
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
