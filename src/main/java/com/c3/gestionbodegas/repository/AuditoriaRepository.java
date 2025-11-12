package com.c3.gestionbodegas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.entities.Auditoria.TipoOperacion;


@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>{

    // Buscar todas las auditorías de un usuario específico
    List<Auditoria> findByUsuario(Usuario usuario);

    // Buscar auditorías por tipo de operación (INSERT, UPDATE, DELETE)
    List<Auditoria> findByTipoOperacion(TipoOperacion tipoOperacion);

    // Buscar auditorías por nombre de entidad afectada (por ejemplo "Producto", "Bodega", etc. )
    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    // Buscar auditorías dentro de un rango de fechas
    List<Auditoria> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    // Consulta personalizada: obtener auditorías filtradas por usuario y tipo
    @Query("""
            SELECT a FROM Auditoria a
            WHERE a.usuario.username = :username
            AND a.tipoOperacion = :tipoOperacion
            ORDER BY a.fechaHora DESC
            """)
    List<Auditoria> buscarPorUsuarioYTipo(String username,TipoOperacion tipoOperacion);
}
