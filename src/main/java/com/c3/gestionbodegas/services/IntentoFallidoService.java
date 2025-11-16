package com.c3.gestionbodegas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.IntentoFallidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IntentoFallidoService {

    @Autowired
    private IntentoFallidoRepository intentoFallidoRepository;

    /**
     * Registrar un intento fallido de movimiento
     */
    public IntentoFallido registrarIntentoFallido(
            IntentoFallido.TipoMovimiento tipoMovimiento,
            String razonError,
            Usuario usuario,
            Producto producto,
            Bodega bodegaOrigen,
            Bodega bodegaDestino,
            Integer cantidadIntentada,
            String detallesAdicionales) {

        IntentoFallido intento = IntentoFallido.builder()
                .tipoMovimiento(tipoMovimiento)
                .razonError(razonError)
                .usuario(usuario)
                .producto(producto)
                .bodegaOrigen(bodegaOrigen)
                .bodegaDestino(bodegaDestino)
                .cantidadIntentada(cantidadIntentada)
                .detallesAdicionales(detallesAdicionales)
                .build();

        return intentoFallidoRepository.save(intento);
    }

    /**
     * Obtener todos los intentos fallidos
     */
    public List<IntentoFallido> obtenerTodos() {
        return intentoFallidoRepository.findAll();
    }

    /**
     * Obtener los últimos 50 intentos fallidos
     */
    public List<IntentoFallido> obtenerUltimos() {
        return intentoFallidoRepository.findTop50ByOrderByFechaHoraDesc();
    }

    /**
     * Obtener intentos fallidos de un usuario
     */
    public List<IntentoFallido> obtenerPorUsuario(Usuario usuario) {
        return intentoFallidoRepository.findByUsuario(usuario);
    }

    /**
     * Obtener intentos fallidos por tipo de movimiento
     */
    public List<IntentoFallido> obtenerPorTipo(IntentoFallido.TipoMovimiento tipo) {
        return intentoFallidoRepository.findByTipoMovimiento(tipo);
    }

    /**
     * Obtener intentos fallidos en un rango de fechas
     */
    public List<IntentoFallido> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return intentoFallidoRepository.findByFechaHoraBetween(inicio, fin);
    }

    /**
     * Obtener intentos fallidos por razón de error (búsqueda)
     */
    public List<IntentoFallido> buscarPorError(String razonError) {
        return intentoFallidoRepository.findByRazonErrorContainingIgnoreCase(razonError);
    }

    /**
     * Obtener un intento fallido por ID
     */
    public IntentoFallido obtenerPorId(Long id) {
        return intentoFallidoRepository.findById(id).orElse(null);
    }

    /**
     * Eliminar un intento fallido por ID
     */
    public boolean eliminar(Long id) {
        if (!intentoFallidoRepository.existsById(id)) return false;
        intentoFallidoRepository.deleteById(id);
        return true;
    }

    /**
     * Limpiar intentos fallidos anteriores a X días
     */
    public void limpiarIntentosDe(Integer diasAtras) {
        LocalDateTime fecha = LocalDateTime.now().minusDays(diasAtras);
        List<IntentoFallido> intentosAntiguos = intentoFallidoRepository
                .findByFechaHoraBetween(LocalDateTime.of(2020, 1, 1, 0, 0), fecha);
        intentoFallidoRepository.deleteAll(intentosAntiguos);
    }
}
