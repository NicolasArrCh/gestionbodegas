package com.c3.gestionbodegas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private final UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodoUsuario() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario); // insert into .... values ...
    }

    public void eliminarUsuario(Long id) {
        UsuarioRepository.deleteById(id);
    }

    public boolean actualizarUsuario(Long id, Usuario usuarioActualizada) {
        int filasActualizadas = usuarioRepository.actualizarUsuario (
            id,
            usuarioActualizada.getNombre());
        
        return filasActualizadas > 0;
    }
}
