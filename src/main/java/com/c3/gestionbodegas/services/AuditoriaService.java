package com.c3.gestionbodegas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Auditoria.TipoOperacion;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    // Obtener todas las auditorías
    public List<Auditoria> obtenerTodas() {
        return auditoriaRepository.findAll();
    }

    // Obtener auditorías paginadas
    public Page<Auditoria> obtenerTodasPaginado(Pageable pageable) {
        return auditoriaRepository.findAll(pageable);
    }

    // Guardar una nueva auditoría
    public Auditoria guardar(Auditoria auditoria) {
        return auditoriaRepository.save(auditoria);
    }

    // Buscar auditorías por usuario (entidad Usuario)
    public List<Auditoria> buscarPorUsuario(Usuario usuario) {
        return auditoriaRepository.findByUsuario(usuario);
    }

    // Buscar auditorías por tipo de operación (INSERT, UPDATE, DELETE)
    public List<Auditoria> buscarPorTipoOperacion(TipoOperacion tipoOperacion) {
        return auditoriaRepository.findByTipoOperacion(tipoOperacion);
    }

    // Buscar auditorías por entidad afectada (por ejemplo "Producto", "Bodega", etc.)
    public List<Auditoria> buscarPorEntidadAfectada(String entidadAfectada) {
        return auditoriaRepository.findByEntidadAfectada(entidadAfectada);
    }

    // Buscar auditorías entre un rango de fechas
    public List<Auditoria> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaRepository.findByFechaHoraBetween(inicio, fin);
    }

    // Buscar auditorías por nombre de usuario y tipo de operación
    public List<Auditoria> buscarPorUsuarioYTipo(String username, TipoOperacion tipoOperacion) {
        return auditoriaRepository.buscarPorUsuarioYTipo(username, tipoOperacion);
    }
}
