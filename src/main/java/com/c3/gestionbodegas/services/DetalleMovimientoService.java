package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.repository.DetalleMovimientoRepository;
import com.c3.gestionbodegas.repository.ProductoRepository;
import com.c3.gestionbodegas.repository.MovimientoInventarioRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetalleMovimientoService {

    @Autowired
    private DetalleMovimientoRepository detalleMovimientoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    // Obtener todos
    public List<DetalleMovimiento> obtenerTodos() {
        return detalleMovimientoRepository.findAll();
    }

    // Obtener por ID (ya no tira error)
    public DetalleMovimiento obtenerPorId(Integer id) {
        return detalleMovimientoRepository.findById(id).orElse(null);
    }

    // Guardar
    @Transactional
    public DetalleMovimiento guardar(DetalleMovimiento detalleMovimiento) {
        // Intentar obtener movimiento completo (para conocer tipo y bodegas)
        MovimientoInventario movimiento = null;
        if (detalleMovimiento.getMovimiento() != null && detalleMovimiento.getMovimiento().getId() != null) {
            movimiento = movimientoInventarioRepository.findById(detalleMovimiento.getMovimiento().getId()).orElse(null);
        }

        // Obtener producto origen (para metadata y stock actual)
        Producto productoOrigen = null;
        if (detalleMovimiento.getProducto() != null && detalleMovimiento.getProducto().getId() != null) {
            productoOrigen = productoRepository.findById(detalleMovimiento.getProducto().getId()).orElse(null);
        }

        Integer cantidad = detalleMovimiento.getCantidad();

        if (movimiento != null && productoOrigen != null && cantidad != null) {
            // Validar que el producto pertenece a la bodega de origen (para SALIDA y TRANSFERENCIA)
            if ((movimiento.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || 
                 movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) &&
                movimiento.getBodegaOrigen() != null) {
                if (productoOrigen.getBodega() == null || !productoOrigen.getBodega().getId().equals(movimiento.getBodegaOrigen().getId())) {
                    throw new IllegalArgumentException("El producto '" + productoOrigen.getNombre() + "' no existe en la bodega de origen. Por favor, selecciona un producto que pertenezca a esta bodega.");
                }
            }

            switch (movimiento.getTipo()) {
                case SALIDA:
                    // Validar stock suficiente y disminuir stock en el producto origen
                    if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
                    if (productoOrigen.getStock() < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente en la bodega origen: disponible=" + productoOrigen.getStock());
                    }
                    productoOrigen.setStock(productoOrigen.getStock() - cantidad);
                    productoRepository.save(productoOrigen);

                    // Si quedó en 0, se conserva el registro en BD con stock=0.
                    // El listado público ocultará productos con stock 0 para que no aparezcan en la página.
                    // No se elimina físicamente aquí para evitar violaciones de integridad referencial.
                    break;

                case ENTRADA:
                    // Validar capacidad de la bodega destino
                    if (movimiento.getBodegaDestino() != null) {
                        Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(movimiento.getBodegaDestino().getId());
                        Integer capacidadDestino = movimiento.getBodegaDestino().getCapacidad() != null ? movimiento.getBodegaDestino().getCapacidad() : 0;
                        if (stockActualDestino + cantidad > capacidadDestino) {
                            throw new IllegalArgumentException("Capacidad insuficiente en la bodega destino. Capacidad: " + capacidadDestino + ", Stock actual: " + stockActualDestino + ", Intentas agregar: " + cantidad);
                        }
                        
                        // Aumentar stock en la bodega destino; crear producto si no existe
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
                    break;

                case TRANSFERENCIA:
                    // Validar stock suficiente y disminuir en origen
                    if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
                    if (productoOrigen.getStock() < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente en la bodega origen: disponible=" + productoOrigen.getStock());
                    }
                    
                    // Validar capacidad de la bodega destino ANTES de realizar el movimiento
                    if (movimiento.getBodegaDestino() != null) {
                        Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(movimiento.getBodegaDestino().getId());
                        Integer capacidadDestino = movimiento.getBodegaDestino().getCapacidad() != null ? movimiento.getBodegaDestino().getCapacidad() : 0;
                        if (stockActualDestino + cantidad > capacidadDestino) {
                            throw new IllegalArgumentException("Capacidad insuficiente en la bodega destino. Capacidad: " + capacidadDestino + ", Stock actual: " + stockActualDestino + ", Intentas agregar: " + cantidad);
                        }
                    }
                    
                    productoOrigen.setStock(productoOrigen.getStock() - cantidad);
                    productoRepository.save(productoOrigen);

                    // Si quedó en 0, se conserva el registro en BD con stock=0.
                    // El listado público ocultará productos con stock 0 para que no aparezcan en la página.
                    // No se elimina físicamente aquí para evitar violaciones de integridad referencial.

                    // Aumentar en destino (crear si no existe)
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
                    break;
            }
        }

        return detalleMovimientoRepository.save(detalleMovimiento);
    }

    // Actualizar
    public DetalleMovimiento actualizar(Integer id, DetalleMovimiento detalleMovimiento) {
        Optional<DetalleMovimiento> existente = detalleMovimientoRepository.findById(id);

        if (existente.isEmpty()) {
            return null;
        }

        DetalleMovimiento detalle = existente.get();

        detalle.setCantidad(detalleMovimiento.getCantidad());
        detalle.setMovimiento(detalleMovimiento.getMovimiento());
        detalle.setProducto(detalleMovimiento.getProducto());

        return detalleMovimientoRepository.save(detalle);
    }

    // Eliminar correctamente
    public boolean eliminar(Integer id) {
        if (!detalleMovimientoRepository.existsById(id)) {
            return false;
        }
        detalleMovimientoRepository.deleteById(id);
        return true;
    }

    // Buscar por movimiento
    public List<DetalleMovimiento> buscarPorMovimiento(MovimientoInventario movimientoInventario) {
        return detalleMovimientoRepository.findByMovimiento(movimientoInventario);
    }

    // Buscar por producto
    public List<DetalleMovimiento> buscarPorProducto(Producto producto) {
        return detalleMovimientoRepository.findByProducto(producto);
    }

    // Buscar por cantidad menor
    public List<DetalleMovimiento> buscarPorCantidadMenorA(Integer cantidad) {
        return detalleMovimientoRepository.findByCantidadLessThan(cantidad);
    }
}