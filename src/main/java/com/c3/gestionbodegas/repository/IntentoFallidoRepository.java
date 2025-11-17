package com.c3.gestionbodegas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.Usuario;

@Repository
public interface IntentoFallidoRepository extends JpaRepository<IntentoFallido, Long> {

    /**
     * Obtener todos los intentos fallidos de un usuario
     */
    List<IntentoFallido> findByUsuario(Usuario usuario);

    /**
     * Obtener intentos fallidos en un rango de fechas
     */
    List<IntentoFallido> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtener intentos fallidos por tipo de movimiento
     */
    List<IntentoFallido> findByTipoMovimiento(IntentoFallido.TipoMovimiento tipo);

    /**
     * Obtener intentos fallidos por razón de error
     */
    List<IntentoFallido> findByRazonErrorContainingIgnoreCase(String razonError);

    /**
     * Obtener los últimos N intentos fallidos ordenados por fecha
     */
    List<IntentoFallido> findTop50ByOrderByFechaHoraDesc();
}
