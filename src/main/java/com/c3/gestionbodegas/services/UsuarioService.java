package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // Buscar un usuario por su ID
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    // Guardar o actualizar un usuario
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Eliminar un usuario por su ID
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }

    // Buscar usuario por username (para login o autenticación)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Verificar si ya existe un usuario con ese username
    public boolean existePorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    // Buscar usuarios por rol
    public List<Usuario> buscarPorRol(Usuario.Rol rol) {
        return usuarioRepository.findByRol(rol);
    }
}
