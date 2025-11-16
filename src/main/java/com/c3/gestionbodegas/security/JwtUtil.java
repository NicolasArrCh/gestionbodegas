package com.c3.gestionbodegas.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.entities.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    // 🔑 Clave secreta (mejor cargarla desde application.properties para prod)
    private final String SECRET_KEY = "EsteEsUnSecretoMuySimplePeroCambiable1234567890";

    // Tiempo de expiración del token (1 hora)
    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    // Generar token a partir del usuario
    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name()); // Guardamos el rol en el token

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getUsername()) // username como subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Obtener username desde el token
    public String obtenerUsername(String token) {
        return obtenerClaims(token).getSubject();
    }

    // Obtener rol desde el token
    public String obtenerRol(String token) {
        return (String) obtenerClaims(token).get("rol");
    }

    // Validar token: username concuerda y no expiró
    public boolean validarToken(String token, Usuario usuario) {
        String username = obtenerUsername(token);
        return (username.equals(usuario.getUsername()) && !estaExpirado(token));
    }

    // Revisar si el token expiró
    private boolean estaExpirado(String token) {
        return obtenerClaims(token).getExpiration().before(new Date());
    }

    // Obtener claims del token
    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}