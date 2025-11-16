package com.c3.gestionbodegas.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.MovimientoInventario.TipoMovimiento;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.MovimientoInventarioService;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin("*")
public class MovimientoInventarioController {

    @Autowired
    private MovimientoInventarioService movimientoService;

    // Obtener todos los movimientos
    @GetMapping
    public List<MovimientoInventario> obtenerTodos() {
        return movimientoService.obtenerTodos();
    }

    // Obtener un movimiento por ID
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoInventario> obtenerPorId(@PathVariable Integer id) {
        Optional<MovimientoInventario> movimiento = movimientoService.buscarPorId(id);
        return movimiento.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    // Crear un nuevo movimiento
    @PostMapping
    public MovimientoInventario crearMovimiento(@RequestBody MovimientoInventario movimiento) {
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