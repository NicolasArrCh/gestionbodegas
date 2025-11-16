package com.c3.gestionbodegas.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.c3.gestionbodegas.services.CustomUserDetailsService;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.services.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, UsuarioService usuarioService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("🔍 Verificando autorización para: " + request.getRequestURI());

        // 1️⃣ Revisamos si hay token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // quitamos "Bearer "
            System.out.println("✅ Token encontrado");

            try {
                String username = jwtUtil.obtenerUsername(token);
                System.out.println("📝 Username del token: " + username);

                // 2️⃣ Cargar usuario y validar token
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);

                    if (usuario != null && jwtUtil.validarToken(token, usuario)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        System.out.println("✅ Token válido para usuario: " + username);
                        System.out.println("🔐 Autoridades: " + userDetails.getAuthorities());

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("❌ Token inválido o usuario no encontrado");
                    }
                }

            } catch (Exception e) {
                // Token inválido o expirado
                System.out.println("❌ Error procesando token: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Sin header Authorization");
        }

        // Continuamos con la chain
        filterChain.doFilter(request, response);
    }
}
