package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;
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
            // Validar que el producto pertenece a la bodega de origen
            if ((movimiento.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA || 
                 movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRANSFERENCIA) &&
                movimiento.getBodegaOrigen() != null) {
                if (productoOrigen.getBodega() == null || !productoOrigen.getBodega().getId().equals(movimiento.getBodegaOrigen().getId())) {
                    throw new IllegalArgumentException("El producto '" + productoOrigen.getNombre() + "' no existe en la bodega de origen.");
                }
            }

            switch (movimiento.getTipo()) {
                case SALIDA:
                    if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
                    if (productoOrigen.getStock() < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente en la bodega origen: disponible=" + productoOrigen.getStock());
                    }
                    productoOrigen.setStock(productoOrigen.getStock() - cantidad);
                    productoRepository.save(productoOrigen);
                    entityManager.flush(); // ⭐ FORZAR FLUSH
                    break;

                case ENTRADA:
                    if (movimiento.getBodegaDestino() != null) {
                        Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(movimiento.getBodegaDestino().getId());
                        Integer capacidadDestino = movimiento.getBodegaDestino().getCapacidad() != null ? movimiento.getBodegaDestino().getCapacidad() : 0;
                        if (stockActualDestino + cantidad > capacidadDestino) {
                            throw new IllegalArgumentException("Capacidad insuficiente en la bodega destino.");
                        }
                        
                        entityManager.flush(); // ⭐ FORZAR FLUSH ANTES DE CONSULTA
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
                        entityManager.flush(); // ⭐ FORZAR FLUSH FINAL
                    }
                    break;

                case TRANSFERENCIA:
                    if (productoOrigen.getStock() == null) productoOrigen.setStock(0);
                    if (productoOrigen.getStock() < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente en la bodega origen: disponible=" + productoOrigen.getStock());
                    }
                    
                    if (movimiento.getBodegaDestino() != null) {
                        Integer stockActualDestino = productoRepository.obtenerStockTotalPorBodega(movimiento.getBodegaDestino().getId());
                        Integer capacidadDestino = movimiento.getBodegaDestino().getCapacidad() != null ? movimiento.getBodegaDestino().getCapacidad() : 0;
                        if (stockActualDestino + cantidad > capacidadDestino) {
                            throw new IllegalArgumentException("Capacidad insuficiente en la bodega destino.");
                        }
                    }
                    
                    productoOrigen.setStock(productoOrigen.getStock() - cantidad);
                    productoRepository.save(productoOrigen);
                    entityManager.flush(); // ⭐ FORZAR FLUSH

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
                        entityManager.flush(); // ⭐ FORZAR FLUSH FINAL
                    }
                    break;
            }
        }

        DetalleMovimiento resultado = detalleMovimientoRepository.save(detalleMovimiento);
        entityManager.flush(); // ⭐ FORZAR FLUSH FINAL
        return resultado;
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