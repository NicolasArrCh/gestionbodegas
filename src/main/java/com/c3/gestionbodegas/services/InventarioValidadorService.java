package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.dto.movimiento.MovimientoDetalleDTO;
import com.c3.gestionbodegas.dto.movimiento.MovimientoRequestDTO;
import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio enfocado exclusivamente en validar las reglas de negocio
 * para movimientos de inventario antes de persistir cualquier cambio (SRP).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioValidadorService {

    private final ProductoRepository productoRepository;

    /**
     * Valida todas las líneas de un movimiento ANTES de aplicar cualquier cambio en stock o persistir cabeceras.
     */
    public void validarMovimientoCompleto(MovimientoRequestDTO request, Bodega bodegaOrigen, Bodega bodegaDestino, List<Producto> productos) {
        log.debug("Validando movimiento de tipo {} con {} detalles", request.getTipo(), request.getDetalles().size());

        if (request.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || request.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) {
            if (bodegaOrigen == null) {
                throw new IllegalArgumentException("La bodega de origen es requerida para movimientos de tipo " + request.getTipo());
            }
        }

        if (request.getTipo() == MovimientoInventario.TipoMovimiento.ENTRADA || request.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) {
            if (bodegaDestino == null) {
                throw new IllegalArgumentException("La bodega de destino es requerida para movimientos de tipo " + request.getTipo());
            }
        }

        if (request.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA && bodegaOrigen != null && bodegaDestino != null) {
            if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
                throw new IllegalArgumentException("La bodega de origen y destino no pueden ser la misma en una transferencia.");
            }
        }

        int incrementoTotalDestino = 0;

        for (int i = 0; i < request.getDetalles().size(); i++) {
            MovimientoDetalleDTO detalle = request.getDetalles().get(i);
            Producto producto = productos.get(i);

            // Validar pertenencia a bodega origen y stock suficiente
            if (request.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || request.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) {
                if (producto.getBodega() == null || !producto.getBodega().getId().equals(bodegaOrigen.getId())) {
                    throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' (ID " + producto.getId() + 
                            ") no pertenece a la bodega de origen " + bodegaOrigen.getNombre());
                }

                int stockActual = producto.getStock() != null ? producto.getStock() : 0;
                if (stockActual < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto '" + producto.getNombre() + 
                            "' en bodega " + bodegaOrigen.getNombre() + ": disponible=" + stockActual + ", solicitado=" + detalle.getCantidad());
                }
            }

            incrementoTotalDestino += detalle.getCantidad();
        }

        // Validar capacidad de bodega destino
        if (bodegaDestino != null && (request.getTipo() == MovimientoInventario.TipoMovimiento.ENTRADA || request.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA)) {
            Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(bodegaDestino.getId());
            if (stockActualDestino == null) stockActualDestino = 0;
            int capacidadDestino = bodegaDestino.getCapacidad() != null ? bodegaDestino.getCapacidad() : 0;

            if (stockActualDestino + incrementoTotalDestino > capacidadDestino) {
                throw new IllegalArgumentException("Capacidad insuficiente en bodega destino '" + bodegaDestino.getNombre() + 
                        "': disponible=" + (capacidadDestino - stockActualDestino) + ", total a ingresar=" + incrementoTotalDestino);
            }
        }
    }
}
