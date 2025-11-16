package com.c3.gestionbodegas.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.c3.gestionbodegas.dto.LoginRequest;
import com.c3.gestionbodegas.dto.LoginResponse;
import com.c3.gestionbodegas.dto.RegisterRequest;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.UsuarioService;
import com.c3.gestionbodegas.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            Usuario usuario = usuarioService.buscarPorUsername(request.getUsername()).orElseThrow();
            String token = jwtUtil.generarToken(usuario);

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Usuario o contraseña inválidos");
        }
    }

    // ✅ REGISTER (opcional)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usuarioService.existePorUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("El username ya existe");
        }

        Usuario nuevo = Usuario.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .nombreCompleto(request.getNombreCompleto())
                .rol(request.getRol() != null ? request.getRol() : Usuario.Rol.OPERADOR)
                .build();

        usuarioService.guardar(nuevo);

        return ResponseEntity.ok("Usuario registrado con éxito");
    }
}
