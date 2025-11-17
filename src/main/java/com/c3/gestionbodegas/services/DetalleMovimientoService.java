package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.DetalleMovimientoRepository;
import com.c3.gestionbodegas.repository.MovimientoInventarioRepository;
import com.c3.gestionbodegas.repository.ProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class DetalleMovimientoService {

    @Autowired
    private DetalleMovimientoRepository detalleMovimientoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private IntentoFallidoService intentoFallidoService;

    @PersistenceContext
    private EntityManager entityManager;

    // Obtener todos
    public List<DetalleMovimiento> obtenerTodos() {
        return detalleMovimientoRepository.findAll();
    }

    // Obtener por ID
    public DetalleMovimiento obtenerPorId(Integer id) {
        return detalleMovimientoRepository.findById(id).orElse(null);
    }

    // Guardar
    @Transactional
    public DetalleMovimiento guardar(DetalleMovimiento detalleMovimiento) {
        // Intentar obtener movimiento completo
        MovimientoInventario movimiento = null;
        if (detalleMovimiento.getMovimiento() != null && detalleMovimiento.getMovimiento().getId() != null) {
            movimiento = movimientoInventarioRepository.findById(detalleMovimiento.getMovimiento().getId()).orElse(null);
        }

        // Obtener producto origen
        Producto productoOrigen = null;
        if (detalleMovimiento.getProducto() != null && detalleMovimiento.getProducto().getId() != null) {
            productoOrigen = productoRepository.findById(detalleMovimiento.getProducto().getId()).orElse(null);
        }

        Integer cantidad = detalleMovimiento.getCantidad();

        if (movimiento != null && productoOrigen != null && cantidad != null) {
            try {
                // ✅ VALIDACIÓN PRIMERO (antes de modificar Producto)
                validarMovimiento(movimiento, productoOrigen, cantidad);

                // ✅ LUEGO aplicar cambios
                switch (movimiento.getTipo()) {
                    case SALIDA:
                        aplicarSalida(movimiento, productoOrigen, cantidad);
                        break;

                    case ENTRADA:
                        aplicarEntrada(movimiento, productoOrigen, cantidad);
                        break;

                    case TRANSFERENCIA:
                        aplicarTransferencia(movimiento, productoOrigen, cantidad);
                        break;
                }
            } catch (IllegalArgumentException e) {
                // ✅ Registrar intento fallido
                registrarIntentoFallido(movimiento, productoOrigen, cantidad, e.getMessage());
                // Re-lanzar excepción para que el controller la maneje
                throw e;
            }
        }

        return detalleMovimientoRepository.save(detalleMovimiento);
    }

    /**
     * Registra un intento fallido en la tabla de intentos_fallidos
     */
    private void registrarIntentoFallido(MovimientoInventario movimiento, Producto producto, 
                                         Integer cantidad, String razonError) {
        try {
            Usuario usuarioActual = obtenerUsuarioActual();
            
            intentoFallidoService.registrarIntentoFallido(
                    convertirTipoMovimiento(movimiento.getTipo()),
                    razonError,
                    usuarioActual,
                    producto,
                    movimiento.getBodegaOrigen(),
                    movimiento.getBodegaDestino(),
                    cantidad,
                    null
            );
        } catch (Exception e) {
            System.err.println("⚠️ Error registrando intento fallido: " + e.getMessage());
        }
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad
     */
    private Usuario obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                // Retornar usuario dummy para no romper el flujo
                Usuario usuario = new Usuario();
                usuario.setId(1);
                usuario.setUsername(username);
                return usuario;
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo usuario actual: " + e.getMessage());
        }
        
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("sistema");
        return usuario;
    }

    /**
     * Convierte TipoMovimiento de MovimientoInventario a IntentoFallido
     */
    private IntentoFallido.TipoMovimiento convertirTipoMovimiento(MovimientoInventario.TipoMovimiento tipo) {
        return switch (tipo) {
            case SALIDA -> IntentoFallido.TipoMovimiento.SALIDA;
            case ENTRADA -> IntentoFallido.TipoMovimiento.ENTRADA;
            case TRANSFERENCIA -> IntentoFallido.TipoMovimiento.TRANSFERENCIA;
        };
    }

    /**
     * Valida TODAS las reglas de negocio ANTES de hacer cambios
     */
    private void validarMovimiento(MovimientoInventario movimiento, Producto productoOrigen, Integer cantidad) {
        // 1️⃣ Validar que el producto pertenece a la bodega de origen (para SALIDA y TRANSFERENCIA)
        if ((movimiento.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || 
             movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) &&
            movimiento.getBodegaOrigen() != null) {
            if (productoOrigen.getBodega() == null || !productoOrigen.getBodega().getId().equals(movimiento.getBodegaOrigen().getId())) {
                throw new IllegalArgumentException("El producto '" + productoOrigen.getNombre() + "' no existe en la bodega de origen.");
            }
        }

        // 2️⃣ Validar stock disponible (para SALIDA y TRANSFERENCIA)
        if ((movimiento.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || 
             movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA)) {
            Integer stockActual = productoOrigen.getStock() != null ? productoOrigen.getStock() : 0;
            if (stockActual < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente en la bodega origen: disponible=" + stockActual);
            }
        }

        // 3️⃣ Validar capacidad bodega destino (para ENTRADA y TRANSFERENCIA)
        if ((movimiento.getTipo() == MovimientoInventario.TipoMovimiento.ENTRADA || 
             movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) &&
            movimiento.getBodegaDestino() != null) {
            Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(movimiento.getBodegaDestino().getId());
            Integer capacidadDestino = movimiento.getBodegaDestino().getCapacidad() != null ? movimiento.getBodegaDestino().getCapacidad() : 0;
            if (stockActualDestino + cantidad > capacidadDestino) {
                throw new IllegalArgumentException("Capacidad insuficiente en la bodega destino: disponible=" + (capacidadDestino - stockActualDestino));
            }
        }
    }

    /**
     * Aplica la operación SALIDA (disminuir stock en bodega origen)
     */
    private void aplicarSalida(MovimientoInventario movimiento, Producto productoOrigen, Integer cantidad) {
        if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
        productoOrigen.setStock(productoOrigen.getStock() - cantidad);
        productoRepository.save(productoOrigen);
    }

    /**
     * Aplica la operación ENTRADA (aumentar stock en bodega destino)
     */
    private void aplicarEntrada(MovimientoInventario movimiento, Producto productoOrigen, Integer cantidad) {
        if (movimiento.getBodegaDestino() != null) {
            Producto productoDestino = productoRepository.findByNombreAndBodega(productoOrigen.getNombre(), movimiento.getBodegaDestino());
            if (productoDestino != null) {
                if (productoDestino.getStock() == null) productoDestino.setStock(0);
                productoDestino.setStock(productoDestino.getStock() + cantidad);
                productoRepository.save(productoDestino);
            } else {
                Producto nuevo = Producto.builder()
                        .nombre(productoOrigen.getNombre())
                        .categoria(productoOrigen.getCategoria())
                        .precio(productoOrigen.getPrecio())
                        .stock(cantidad)
                        .bodega(movimiento.getBodegaDestino())
                        .build();
                productoRepository.save(nuevo);
            }
        }
    }

    /**
     * Aplica la operación TRANSFERENCIA (disminuir en origen, aumentar en destino)
     */
    private void aplicarTransferencia(MovimientoInventario movimiento, Producto productoOrigen, Integer cantidad) {
        // Disminuir en bodega origen
        if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
        productoOrigen.setStock(productoOrigen.getStock() - cantidad);
        productoRepository.save(productoOrigen);

        // Aumentar en bodega destino
        if (movimiento.getBodegaDestino() != null) {
            Producto prodDest = productoRepository.findByNombreAndBodega(productoOrigen.getNombre(), movimiento.getBodegaDestino());
            if (prodDest != null) {
                if (prodDest.getStock() == null) prodDest.setStock(0);
                prodDest.setStock(prodDest.getStock() + cantidad);
                productoRepository.save(prodDest);
            } else {
                Producto nuevo = Producto.builder()
                        .nombre(productoOrigen.getNombre())
                        .categoria(productoOrigen.getCategoria())
                        .precio(productoOrigen.getPrecio())
                        .stock(cantidad)
                        .bodega(movimiento.getBodegaDestino())
                        .build();
                productoRepository.save(nuevo);
            }
        }
    }

    // Resto de métodos sin cambios...
    
    public DetalleMovimiento actualizar(Integer id, DetalleMovimiento detalleMovimiento) {
        Optional<DetalleMovimiento> existente = detalleMovimientoRepository.findById(id);
        if (existente.isEmpty()) return null;

        DetalleMovimiento detalle = existente.get();
        detalle.setCantidad(detalleMovimiento.getCantidad());
        detalle.setMovimiento(detalleMovimiento.getMovimiento());
        detalle.setProducto(detalleMovimiento.getProducto());

        return detalleMovimientoRepository.save(detalle);
    }

    public boolean eliminar(Integer id) {
        if (!detalleMovimientoRepository.existsById(id)) return false;
        detalleMovimientoRepository.deleteById(id);
        return true;
    }

    public List<DetalleMovimiento> buscarPorMovimiento(MovimientoInventario movimientoInventario) {
        return detalleMovimientoRepository.findByMovimiento(movimientoInventario);
    }

    public List<DetalleMovimiento> buscarPorProducto(Producto producto) {
        return detalleMovimientoRepository.findByProducto(producto);
    }

    public List<DetalleMovimiento> buscarPorCantidadMenorA(Integer cantidad) {
        return detalleMovimientoRepository.findByCantidadLessThan(cantidad);
    }
}