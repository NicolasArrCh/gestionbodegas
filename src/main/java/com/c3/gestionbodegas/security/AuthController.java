package com.c3.gestionbodegas.security;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            Usuario usuario = usuarioService.buscarPorUsername(request.getUsername()).orElseThrow();
            String token = jwtUtil.generarToken(usuario);

            log.info("Login exitoso para usuario: {}", request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (AuthenticationException e) {
            log.warn("Fallo de autenticación para usuario: {}", request.getUsername());
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

        usuarioService.crear(nuevo);

        log.info("Usuario registrado con éxito: {}", request.getUsername());
        return ResponseEntity.ok("Usuario registrado con éxito");
    }
}
