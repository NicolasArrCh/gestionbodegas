package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public List<Auditoria> obtenerTodoAuditoria() {
        return auditoriaRepository.findAll();
    }

    public Auditoria buscarAuditoriaPorId(Long id) {
        return auditoriaRepository.findById(id).orElse(null);
    }

    public List<Auditoria> buscarPorNombre(String nombre) {
        return auditoriaRepository.findByNombre(nombre);
    }

    public Auditoria guardarAuditoria(Auditoria auditoria) {
        return auditoriaRepository.save(auditoria); // insert into .... values ...
    }

    public void eliminarAuditoria(Long id) {
        auditoriaRepository.deleteById(id);
    }

    public boolean actualizarAuditoria(Long id, Auditoria auditoriaActualizada) {
        int filasActualizadas = auditoriaRepository.actualizarAuditoria (
            id,
            auditoriaActualizada.getNombre());
        
        return filasActualizadas > 0;
    }
    
}
