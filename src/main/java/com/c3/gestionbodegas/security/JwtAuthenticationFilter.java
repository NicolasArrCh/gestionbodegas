package com.c3.gestionbodegas.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.CustomUserDetailsService;
import com.c3.gestionbodegas.services.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        log.debug("Verificando autorización para: {}", request.getRequestURI());

        // 1️⃣ Revisamos si hay token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // quitamos "Bearer "
            log.debug("Token JWT encontrado en request");

            try {
                String username = jwtUtil.obtenerUsername(token);
                log.debug("Username del token: {}", username);

                // 2️⃣ Cargar usuario y validar token
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);

                    if (usuario != null && jwtUtil.validarToken(token, usuario)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.debug("Token válido para usuario: {}, Autoridades: {}", username, userDetails.getAuthorities());

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        log.warn("Token inválido o usuario no encontrado para username: {}", username);
                    }
                }

            } catch (Exception e) {
                log.error("Error procesando token JWT: {}", e.getMessage());
            }
        } else {
            log.trace("Sin header Authorization para: {}", request.getRequestURI());
        }

        // Continuamos con la chain
        filterChain.doFilter(request, response);
    }
}
