package com.c3.gestionbodegas.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.dto.mapper.MovimientoMapper;
import com.c3.gestionbodegas.dto.movimiento.MovimientoDetalleDTO;
import com.c3.gestionbodegas.dto.movimiento.MovimientoRequestDTO;
import com.c3.gestionbodegas.dto.movimiento.MovimientoResponseDTO;
import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.exception.ResourceNotFoundException;
import com.c3.gestionbodegas.repository.BodegaRepository;
import com.c3.gestionbodegas.repository.DetalleMovimientoRepository;
import com.c3.gestionbodegas.repository.MovimientoInventarioRepository;
import com.c3.gestionbodegas.repository.ProductoRepository;
import com.c3.gestionbodegas.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio orquestador para ejecutar movimientos de inventario de forma completamente atómica (DT2).
 * Todas las operaciones (cabecera + detalles + actualización de stock) ocurren en una única transacción.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovimientoOrquestadorService {

    private final MovimientoInventarioRepository movimientoRepo;
    private final DetalleMovimientoRepository detalleRepo;
    private final ProductoRepository productoRepo;
    private final BodegaRepository bodegaRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioValidadorService validador;
    private final DetalleMovimientoService detalleMovimientoService;
    private final IntentoFallidoService intentoFallidoService;
    private final SecurityContextService securityContextService;
    private final MovimientoMapper movimientoMapper;

    @Transactional(rollbackFor = Exception.class)
    public MovimientoResponseDTO ejecutarMovimiento(MovimientoRequestDTO request, String username) {
        log.info("Iniciando ejecución atómica de movimiento de tipo {} por usuario {}", request.getTipo(), username);

        // 1. Resolver usuario autenticado
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseGet(securityContextService::obtenerUsuarioActual);

        // 2. Resolver bodegas
        Bodega bodegaOrigen = null;
        if (request.getBodegaOrigenId() != null) {
            bodegaOrigen = bodegaRepo.findById(request.getBodegaOrigenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega origen no encontrada con ID: " + request.getBodegaOrigenId()));
        }

        Bodega bodegaDestino = null;
        if (request.getBodegaDestinoId() != null) {
            bodegaDestino = bodegaRepo.findById(request.getBodegaDestinoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega destino no encontrada con ID: " + request.getBodegaDestinoId()));
        }

        // 3. Cargar todos los productos referenciados
        List<Producto> productos = new ArrayList<>();
        for (MovimientoDetalleDTO d : request.getDetalles()) {
            Producto prod = productoRepo.findById(d.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + d.getProductoId()));
            productos.add(prod);
        }

        // 4. VALIDAR TODAS las líneas ANTES de modificar datos (evita modificaciones parciales)
        try {
            validador.validarMovimientoCompleto(request, bodegaOrigen, bodegaDestino, productos);
        } catch (IllegalArgumentException ex) {
            log.warn("Fallo de validación en movimiento atómico: {}", ex.getMessage());
            // Registrar intento fallido
            intentoFallidoService.registrarIntentoFallido(
                    convertirTipoMovimiento(request.getTipo()),
                    ex.getMessage(),
                    usuario,
                    productos.isEmpty() ? null : productos.get(0),
                    bodegaOrigen,
                    bodegaDestino,
                    request.getDetalles().isEmpty() ? 0 : request.getDetalles().get(0).getCantidad(),
                    null
            );
            throw ex;
        }

        // 5. Crear y persistir cabecera del movimiento
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .tipo(request.getTipo())
                .usuario(usuario)
                .bodegaOrigen(bodegaOrigen)
                .bodegaDestino(bodegaDestino)
                .build();
        movimiento = movimientoRepo.save(movimiento);

        // 6. Procesar cada detalle y actualizar stock
        List<DetalleMovimiento> detallesGuardados = new ArrayList<>();
        for (int i = 0; i < request.getDetalles().size(); i++) {
            MovimientoDetalleDTO d = request.getDetalles().get(i);
            Producto prod = productos.get(i);

            switch (request.getTipo()) {
                case SALIDA -> detalleMovimientoService.aplicarSalida(movimiento, prod, d.getCantidad());
                case ENTRADA -> detalleMovimientoService.aplicarEntrada(movimiento, prod, d.getCantidad());
                case TRANSFERENCIA -> detalleMovimientoService.aplicarTransferencia(movimiento, prod, d.getCantidad());
            }

            DetalleMovimiento detalle = DetalleMovimiento.builder()
                    .movimiento(movimiento)
                    .producto(prod)
                    .cantidad(d.getCantidad())
                    .build();
            detallesGuardados.add(detalleRepo.save(detalle));
        }

        log.info("Movimiento atómico ejecutado exitosamente con ID {} y {} detalles", movimiento.getId(), detallesGuardados.size());
        return movimientoMapper.toResponseDTO(movimiento, detallesGuardados);
    }

    private IntentoFallido.TipoMovimiento convertirTipoMovimiento(MovimientoInventario.TipoMovimiento tipo) {
        return switch (tipo) {
            case SALIDA -> IntentoFallido.TipoMovimiento.SALIDA;
            case ENTRADA -> IntentoFallido.TipoMovimiento.ENTRADA;
            case TRANSFERENCIA -> IntentoFallido.TipoMovimiento.TRANSFERENCIA;
        };
    }
}
