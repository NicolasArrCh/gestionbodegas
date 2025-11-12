package com.c3.gestionbodegas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.UsuarioService;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private final UsuarioService usuarioService;

    // Devolver todas los usuarios
    @GetMapping
    public ResponseEntity<Usuario> obtenerTodoUsuario() {
        List<Usuario> usuario = usuarioService.obtenerTodoUsuario();
        return usuario.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuario);
    }

    // Buscar por el nombre
    @GetMapping("/buscar")
    public ResponseEntity<Usuario> buscarPorNombre(@RequestParam String nombre) {
        List<Usuario> usuario = usuarioService.buscarPorNombre(nombre);
        return usuario.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuario);
    }

    // Buscar por ID
     @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return usuario != null ? ResponseEntity.ok(usuario) : ResponseEntity.ok(usuario);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioNuevo = usuarioService.guardarUsuario(usuario);

        return ResponseEntity.ok(usuarioNuevo);
    }

    // Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        boolean eliminado = usuarioService.eliminarUsuario(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Endpoind del patch
    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizarUsuario(
        @PathVariable Long id,
        @RequestBody Usuario usuarioActualizado) {

            boolean actualizado = usuarioService.actualizarUsuario(id, usuarioActualizado);
            return actualizado ?
                ResponseEntity.ok("Usuario actualizado con éxito.") :
                ResponseEntity.notFound().build();
        }
}
