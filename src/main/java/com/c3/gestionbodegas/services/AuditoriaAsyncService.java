package com.c3.gestionbodegas.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaAsyncService {

    private final AuditoriaRepository auditoriaRepository;

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
            log.debug("Auditoría {} registrada para: {}", tipo, entidad);
        } catch (Exception e) {
            log.error("Error al guardar auditoría: {}", e.getMessage(), e);
        }
    }
}