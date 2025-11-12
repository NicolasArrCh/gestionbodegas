package com.c3.gestionbodegas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.c3.gestionbodegas.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

    // Buscar usuario por username (para login)
    Optional <Usuario> findByUsername(String username);

    // Verificar si ya existe un usuario con ese username
    boolean existsByUsername(String username);

    // Buscar usuarios por rol
    List<Usuario> findByRol(Usuario.Rol rol);

}
