package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.dto.movimiento.MovimientoRequestDTO;
import com.c3.gestionbodegas.dto.movimiento.MovimientoResponseDTO;
import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.MovimientoInventario.TipoMovimiento;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.MovimientoInventarioService;
import com.c3.gestionbodegas.services.MovimientoOrquestadorService;
import com.c3.gestionbodegas.services.SecurityContextService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;
    private final MovimientoOrquestadorService orquestadorService;
    private final SecurityContextService securityContextService;

    // ✅ Nuevo endpoint unificado para movimientos atómicos (DT2)
    @PostMapping("/ejecutar")
    public ResponseEntity<MovimientoResponseDTO> ejecutarMovimiento(
            @Valid @RequestBody MovimientoRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : securityContextService.obtenerUsernameActual();
        log.info("Petición de movimiento atómico recibida de usuario: {}", username);
        MovimientoResponseDTO response = orquestadorService.ejecutarMovimiento(request, username);
        return ResponseEntity.ok(response);
    }

    // Obtener todos los movimientos
    @GetMapping
    public List<MovimientoInventario> obtenerTodos() {
        return movimientoService.obtenerTodos();
    }

    // Obtener todos los movimientos paginados (DT9)
    @GetMapping("/paginado")
    public ResponseEntity<Page<MovimientoInventario>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(movimientoService.obtenerTodosPaginado(pageable));
    }

    // Obtener un movimiento por ID
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoInventario> obtenerPorId(@PathVariable Integer id) {
        Optional<MovimientoInventario> movimiento = movimientoService.buscarPorId(id);
        return movimiento.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    // Crear un nuevo movimiento (legacy, preferir /ejecutar)
    @Deprecated
    @PostMapping
    public MovimientoInventario crearMovimiento(@RequestBody MovimientoInventario movimiento) {
        log.info("Creando cabecera de movimiento legacy");
        return movimientoService.guardar(movimiento);
    }

    // Actualizar un movimiento existente
    @PutMapping("/{id}")
    public ResponseEntity<MovimientoInventario> actualizarMovimiento(
            @PathVariable Integer id,
            @RequestBody MovimientoInventario movimientoActualizado) {
        Optional<MovimientoInventario> movimientoExistente = movimientoService.buscarPorId(id);
        if (movimientoExistente.isPresent()) {
            movimientoActualizado.setId(id);
            MovimientoInventario actualizado = movimientoService.guardar(movimientoActualizado);
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Eliminar un movimiento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Integer id) {
        Optional<MovimientoInventario> movimiento = movimientoService.buscarPorId(id);
        if (movimiento.isPresent()) {
            movimientoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Filtrar movimientos por tipo
    @GetMapping("/tipo/{tipo}")
    public List<MovimientoInventario> buscarPorTipo(@PathVariable TipoMovimiento tipo) {
        return movimientoService.buscarPorTipo(tipo);
    }

    // Filtrar movimientos por usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<MovimientoInventario> buscarPorUsuario(@PathVariable Integer usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return movimientoService.buscarPorUsuario(usuario);
    }

    // Filtrar movimientos por rango de fechas
    @GetMapping("/rango-fechas")
    public List<MovimientoInventario> buscarPorRangoDeFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return movimientoService.buscarPorRangoDeFechas(inicio, fin);
    }

    // Filtrar movimientos por bodega origen o destino
    @GetMapping("/bodegas")
    public List<MovimientoInventario> buscarPorBodegas(
            @RequestParam Integer bodegaOrigenId,
            @RequestParam Integer bodegaDestinoId) {
        Bodega origen = new Bodega();
        origen.setId(bodegaOrigenId);
        Bodega destino = new Bodega();
        destino.setId(bodegaDestinoId);
        return movimientoService.buscarPorBodegaOrigenODestino(origen, destino);
    }

    // Obtener productos más movidos
    @GetMapping("/productos-mas-movidos")
    public List<Object[]> productosMasMovidos() {
        return movimientoService.obtenerProductosMasMovidos();
    }
}