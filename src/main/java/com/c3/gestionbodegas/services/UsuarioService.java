package com.c3.gestionbodegas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.exception.ResourceNotFoundException;
import com.c3.gestionbodegas.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // Obtener usuarios paginados
    public Page<Usuario> obtenerTodosPaginado(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    // Buscar un usuario por su ID
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Crea un nuevo usuario. Encripta la contraseña proporcionada.
     */
    public Usuario crear(Usuario usuario) {
        log.info("Creando nuevo usuario: {}", usuario.getUsername());
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            String passEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passEncriptada);
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente. Solo encripta la contraseña
     * si se proporcionó una nueva (no vacía y diferente al hash actual).
     */
    public Usuario actualizar(Integer id, Usuario datosNuevos) {
        log.info("Actualizando usuario con ID: {}", id);
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (datosNuevos.getNombreCompleto() != null) {
            existente.setNombreCompleto(datosNuevos.getNombreCompleto());
        }
        if (datosNuevos.getRol() != null) {
            existente.setRol(datosNuevos.getRol());
        }
        if (datosNuevos.getUsername() != null && !datosNuevos.getUsername().isBlank()) {
            existente.setUsername(datosNuevos.getUsername());
        }

        // Solo re-encriptar si se envió una contraseña nueva (no vacía y no encriptada previamente con BCrypt)
        if (datosNuevos.getPassword() != null
                && !datosNuevos.getPassword().isBlank()
                && !datosNuevos.getPassword().startsWith("$2a$")) {
            existente.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
        }

        return usuarioRepository.save(existente);
    }

    /**
     * Método legacy mantenido por compatibilidad.
     * Delega a actualizar si existe ID, o a crear si es nuevo.
     */
    @Deprecated
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() != null && usuarioRepository.existsById(usuario.getId())) {
            return actualizar(usuario.getId(), usuario);
        }
        return crear(usuario);
    }

    // Eliminar un usuario por su ID
    public void eliminar(Integer id) {
        log.info("Eliminando usuario con ID: {}", id);
        usuarioRepository.deleteById(id);
    }

    // Buscar usuario por username
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