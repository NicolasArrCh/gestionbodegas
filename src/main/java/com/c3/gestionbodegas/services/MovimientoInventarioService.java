package com.c3.gestionbodegas.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.MovimientoInventario.TipoMovimiento;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.MovimientoInventarioRepository;

@Service
public class MovimientoInventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    // Obtener todos los movimientos
    public List<MovimientoInventario> obtenerTodos() {
        return movimientoInventarioRepository.findAll();
    }

    // Buscar un movimiento por ID
    public Optional<MovimientoInventario> buscarPorId(Integer id) {
        return movimientoInventarioRepository.findById(id);
    }

    // Guardar o actualizar un movimiento
    public MovimientoInventario guardar(MovimientoInventario movimiento) {
        return movimientoInventarioRepository.save(movimiento);
    }

    // Eliminar un movimiento por su ID
    public void eliminar(Integer id) {
        movimientoInventarioRepository.deleteById(id);
    }

    // Buscar movimientos por tipo (ENTRADA, SALIDA, TRANSFERENCIA)
    public List<MovimientoInventario> buscarPorTipo(TipoMovimiento tipo) {
        return movimientoInventarioRepository.findByTipo(tipo);
    }

    // Buscar movimientos por usuario responsable
    public List<MovimientoInventario> buscarPorUsuario(Usuario usuario) {
        return movimientoInventarioRepository.findByUsuario(usuario);
    }

    // Buscar movimientos entre un rango de fechas
    public List<MovimientoInventario> buscarPorRangoDeFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // ⚠️ Si el método del repository es findByFecha(...) y no findByFechaBetween(...),
        // deberías cambiar el nombre del método en el repository a findByFechaBetween para que funcione correctamente.
        return movimientoInventarioRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    // Buscar movimientos por bodega de origen o destino
    public List<MovimientoInventario> buscarPorBodegaOrigenODestino(Bodega bodegaOrigen, Bodega bodegaDestino) {
        return movimientoInventarioRepository.findByBodegaOrigenOrBodegaDestino(bodegaOrigen, bodegaDestino);
    }

    // Obtener reporte de los productos más movidos
    public List<Object[]> obtenerProductosMasMovidos() {
        return movimientoInventarioRepository.obtenerProductosMasMovidos();
    }
}
