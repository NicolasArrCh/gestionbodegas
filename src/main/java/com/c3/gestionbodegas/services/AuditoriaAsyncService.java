package com.c3.gestionbodegas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.AuditoriaRepository;

@Service
public class AuditoriaAsyncService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarAuditoriaAsync(Auditoria.TipoOperacion tipo, Usuario usuario, 
                                       String entidad, String valorAnt, String valorNue) {
        try {
            Auditoria auditoria = Auditoria.builder()
                    .tipoOperacion(tipo)
                    .usuario(usuario)
                    .entidadAfectada(entidad)
                    .valorAnterior(valorAnt)
                    .valorNuevo(valorNue)
                    .build();
            
            auditoriaRepository.save(auditoria);
            System.out.println("✅ Auditoría " + tipo + " registrada para: " + entidad);
        } catch (Exception e) {
            System.err.println("❌ Error al guardar auditoría: " + e.getMessage());
        }
    }
}