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

    public Auditoria buscarPorId(Long id) {
        return auditoriaRepository.findById(id).orElse(null);
    }

    public List<Auditoria> buscarPorNombre(String nombre) {
        return auditoriaRepository.findByNombre(nombre);
    }

    public Auditoria guardarAuditoria(Auditoria auditoria) {
        return auditoriaRepository.save(auditoria); // insert into .... values ...
    }

    public boolean eliminarAuditoria(Long id) {
        if (auditoriaRepository.existsById(id)) {
            auditoriaRepository.deleteById(id);
            return true;
        }

        return false;
    }

    public boolean actualizarAuditoria(Long id, Auditoria auditoriaActualizada) {
        int filasActualizadas = auditoriaRepository.actualizarAuditoria (
            id,
            auditoriaActualizada.getUsername(),
            auditoriaActualizada.getTipoOperacion());
        
        return filasActualizadas > 0;
    }
    
}
