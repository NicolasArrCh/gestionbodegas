package com.c3.gestionbodegas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.c3.gestionbodegas.services.CustomUserDetailsService;
import com.c3.gestionbodegas.services.UsuarioService;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    // 1️⃣ PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2️⃣ AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3️⃣ JWT Authentication Filter Bean
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(@Lazy UsuarioService usuarioService) {
        return new JwtAuthenticationFilter(jwtUtil, customUserDetailsService, usuarioService);
    }

    // 4️⃣ Configuración de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> {}) // Habilitar CORS (usa la configuración de CorsConfig)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // endpoints públicos (para login y registro)
                .requestMatchers("/api/auth/**").permitAll()
                // HTML públicos (login y dashboard_admin)
                .requestMatchers("/html/**").permitAll()
                // Swagger y OpenAPI públicos
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Archivos estáticos (CSS, JS, etc) públicos
                .requestMatchers("/css/**", "/js/**").permitAll()
                // API requiere autenticación
                .requestMatchers("/api/**").authenticated()
                // cualquier otra ruta requiere token
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.disable()) // JWT es stateless
            // 🔑 Añadir el filtro JWT AQUI
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
