# 🔴 Plan de Implementación 1: Vulnerabilidades Críticas de Seguridad

**Proyecto:** Sistema de Gestión de Bodegas  
**Prioridad:** Máxima  
**Principios Rectores:** SOLID + Modularización estricta  
**Prerequisito:** Ninguno — este plan se ejecuta primero.

---

## Principios SOLID Aplicados en este Plan

| Principio | Aplicación Concreta |
| :--- | :--- |
| **S – Single Responsibility** | Cada clase de seguridad tiene UNA sola razón de cambio: `JwtUtil` solo gestiona tokens, `SecurityConfig` solo configura permisos HTTP, `AuthController` solo orquesta autenticación. |
| **O – Open/Closed** | El sistema de permisos usa anotaciones declarativas (`@PreAuthorize`) que se pueden extender sin modificar la lógica del filtro JWT. |
| **L – Liskov Substitution** | Los DTOs de respuesta sustituyen a las entidades en los contratos de API sin romper el comportamiento esperado por el cliente. |
| **I – Interface Segregation** | Se separan las interfaces de servicio: `AuthService` (autenticación) vs `UsuarioService` (gestión CRUD), en lugar de tener un servicio monolítico. |
| **D – Dependency Inversion** | Los controladores dependen de abstracciones (interfaces de servicios), no de implementaciones concretas. La configuración JWT se inyecta desde `application.properties`, no se hardcodea. |

---

## Tarea V1: Activar Protección Real de Endpoints en Spring Security

### Problema Actual
En [`SecurityConfig.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/SecurityConfig.java) (líneas 52-57):
```java
.requestMatchers("/api/**").permitAll()  // TODO público
.anyRequest().permitAll()  // Todo lo demás también público
```
Todos los endpoints están abiertos. El filtro JWT existe pero Spring Security no exige autenticación en ningún recurso protegido.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`SecurityConfig.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/SecurityConfig.java) — Definir política de acceso granular por endpoint y rol.

#### Cambios Específicos
Reemplazar la configuración `.authorizeHttpRequests(...)` con una política de permisos por capas:

```java
.authorizeHttpRequests(auth -> auth
    // ── Recursos públicos (sin autenticación) ──
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/html/**", "/css/**", "/js/**").permitAll()
    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
    .requestMatchers("/actuator/health").permitAll()

    // ── Endpoints de solo lectura: ADMIN, ENCARGADO, OPERADOR ──
    .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/bodegas/**").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/movimientos/**").authenticated()

    // ── Escritura en inventario: ADMIN y ENCARGADO ──
    .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("ADMIN", "ENCARGADO")
    .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("ADMIN", "ENCARGADO")
    .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyRole("ADMIN", "ENCARGADO")
    .requestMatchers(HttpMethod.POST, "/api/movimientos/**").hasAnyRole("ADMIN", "ENCARGADO")
    .requestMatchers(HttpMethod.POST, "/api/detalle-movimientos/**").hasAnyRole("ADMIN", "ENCARGADO")

    // ── Gestión de bodegas: solo ADMIN ──
    .requestMatchers(HttpMethod.POST, "/api/bodegas/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/bodegas/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/bodegas/**").hasRole("ADMIN")

    // ── Gestión de usuarios: solo ADMIN ──
    .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

    // ── Auditoría: solo lectura para ADMIN ──
    .requestMatchers(HttpMethod.GET, "/api/auditorias/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/api/intentos-fallidos/**").hasAnyRole("ADMIN", "ENCARGADO")

    // ── Reportes: ADMIN y ENCARGADO ──
    .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "ENCARGADO")

    // ── Todo lo demás requiere autenticación ──
    .anyRequest().authenticated()
)
```

#### Principio SOLID
- **SRP**: `SecurityConfig` se limita exclusivamente a la definición de políticas HTTP. No contiene lógica de negocio.
- **OCP**: Los permisos se agregan declarativamente sin modificar el código del filtro JWT.

### Criterio de Aceptación
- `GET /api/productos` sin token → **401 Unauthorized**
- `GET /api/productos` con token de OPERADOR → **200 OK**
- `POST /api/bodegas` con token de OPERADOR → **403 Forbidden**
- `POST /api/bodegas` con token de ADMIN → **200 OK**
- `POST /api/auth/login` sin token → **200 OK** (público)

---

## Tarea V2: Corregir Escalamiento de Privilegios en Registro

### Problema Actual
En [`AuthController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/AuthController.java) (líneas 50-66), el endpoint `POST /api/auth/register` recibe un `RegisterRequest` que incluye el campo `rol`. Cualquier usuario puede enviar `"rol": "ADMIN"` y crearse una cuenta con permisos totales.

En [`RegisterRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/RegisterRequest.java) (línea 15):
```java
private Rol rol;  // El cliente define su propio rol
```

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`RegisterRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/RegisterRequest.java) — Eliminar el campo `rol` del DTO público de registro.
- **[MODIFY]** [`AuthController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/AuthController.java) — Forzar `Rol.OPERADOR` en el auto-registro. Agregar validación `@Valid`.
- **[NEW]** `dto/AdminCreateUserRequest.java` — DTO separado exclusivo para que un ADMIN cree usuarios con cualquier rol.
- **[MODIFY]** [`UsuarioController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/UsuarioController.java) — Usar `AdminCreateUserRequest` en `POST /api/usuarios`, protegido con `@PreAuthorize("hasRole('ADMIN')")`.

#### Cambios Específicos

**`RegisterRequest.java`** — Eliminar campo `rol`:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    // ❌ Sin campo "rol" — el auto-registro siempre es OPERADOR
}
```

**`AuthController.register()`** — Forzar `Rol.OPERADOR`:
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    // ...
    Usuario nuevo = Usuario.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .nombreCompleto(request.getNombreCompleto())
            .rol(Usuario.Rol.OPERADOR) // ← SIEMPRE OPERADOR
            .build();
    // ...
}
```

**`AdminCreateUserRequest.java`** (NUEVO):
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserRequest {
    @NotBlank private String username;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String nombreCompleto;
    @NotNull private Usuario.Rol rol;  // Solo un ADMIN puede definir el rol
}
```

#### Principio SOLID
- **SRP**: Dos DTOs con responsabilidades distintas: `RegisterRequest` para auto-registro público, `AdminCreateUserRequest` para gestión administrativa.
- **ISP**: El cliente público no ve ni interactúa con el campo `rol`. Solo la interfaz del admin lo expone.

### Criterio de Aceptación
- `POST /api/auth/register` con `{"username":"x", "password":"123456", "nombreCompleto":"Test", "rol":"ADMIN"}` → el campo `rol` es **ignorado**, el usuario se crea como `OPERADOR`.
- `POST /api/usuarios` sin token o con token no-ADMIN → **401/403**.
- `POST /api/usuarios` con token ADMIN y `AdminCreateUserRequest` → se crea el usuario con el rol especificado.

---

## Tarea V3: Eliminar Fuga de Hashes de Contraseña en Respuestas API

### Problema Actual
Los endpoints `GET /api/usuarios` y `GET /api/usuarios/{id}` retornan la entidad [`Usuario.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Usuario.java) completa, incluyendo el campo `password` con el hash BCrypt. Esto expone datos sensibles.

### Solución Propuesta

#### Archivos Nuevos y Modificados
- **[NEW]** `dto/UsuarioResponseDTO.java` — DTO de salida que excluye el campo `password`.
- **[NEW]** `dto/mapper/UsuarioMapper.java` — Mapper dedicado a convertir `Usuario → UsuarioResponseDTO`.
- **[MODIFY]** [`UsuarioController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/UsuarioController.java) — Retornar `UsuarioResponseDTO` en lugar de `Usuario`.

#### Cambios Específicos

**`UsuarioResponseDTO.java`** (NUEVO):
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponseDTO {
    private Integer id;
    private String username;
    private String nombreCompleto;
    private String rol;
    private LocalDateTime fechaCreacion;
}
```

**`UsuarioMapper.java`** (NUEVO):
```java
@Component
public class UsuarioMapper {
    public UsuarioResponseDTO toDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    public List<UsuarioResponseDTO> toDTOList(List<Usuario> usuarios) {
        return usuarios.stream().map(this::toDTO).toList();
    }
}
```

**`UsuarioController.java`** — Cambiar tipo de retorno:
```java
@GetMapping
public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
    List<Usuario> usuarios = usuarioService.obtenerTodos();
    return ResponseEntity.ok(usuarioMapper.toDTOList(usuarios));
}
```

#### Principio SOLID
- **SRP**: `UsuarioMapper` tiene una sola responsabilidad: transformar entidades a DTOs. No contiene lógica de negocio ni de persistencia.
- **DIP**: El controlador depende de la abstracción `UsuarioResponseDTO` (contrato público) y no de la entidad JPA interna `Usuario`.
- **OCP**: Si mañana se necesita agregar un campo al DTO (como `email`), se modifica el mapper sin tocar el controlador ni la entidad.

### Criterio de Aceptación
- `GET /api/usuarios` → respuesta JSON **NO** contiene el campo `password`.
- `GET /api/usuarios/{id}` → respuesta JSON **NO** contiene el campo `password`.
- La funcionalidad del frontend no se rompe (los campos `id`, `username`, `nombreCompleto`, `rol` siguen presentes).

---

## Tarea V4: Externalizar Secreto JWT desde Configuración

### Problema Actual
En [`JwtUtil.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/JwtUtil.java) (líneas 18-22):
```java
private final String SECRET_KEY = "EsteEsUnSecretoMuySimplePeroCambiable1234567890";
private final long EXPIRATION_TIME = 1000 * 60 * 60;
```
El secreto JWT está hardcodeado directamente en el código fuente. Si el repositorio es público, cualquiera puede forjar tokens válidos.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`JwtUtil.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/JwtUtil.java) — Inyectar secreto y expiración desde `@Value`.
- **[MODIFY]** [`application.properties`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/resources/application.properties) — Documentar propiedades con valores por defecto seguros.

#### Cambios Específicos

**`JwtUtil.java`**:
```java
@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationTimeMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        this.secretKey = secretKey;
        this.expirationTimeMs = expirationMinutes * 60 * 1000;
    }
    // ... resto de métodos sin cambios, usando this.secretKey y this.expirationTimeMs
}
```

**`application.properties`** — ya tiene las propiedades pero `JwtUtil` no las usa:
```properties
# Estas propiedades ya existen; ahora sí serán leídas por JwtUtil
jwt.secret=${JWT_SECRET:CambiarEstoEnProduccion_ClaveSegura2026}
jwt.expiration-minutes=${JWT_EXPIRATION:60}
```

#### Principio SOLID
- **SRP**: `JwtUtil` solo gestiona generación/validación de tokens. La configuración la recibe externamente.
- **DIP**: `JwtUtil` depende de una abstracción (las propiedades de Spring) en lugar de valores concretos hardcodeados.

### Criterio de Aceptación
- El proyecto compila y los tokens siguen funcionando con los valores por defecto de `application.properties`.
- Si se define la variable de entorno `JWT_SECRET=MiClavePersonalizada`, `JwtUtil` la usa automáticamente.
- No existe ningún valor secreto hardcodeado en el código fuente.

---

## Tarea V5: Proteger Endpoint de Auditoría contra Inyección de Registros Falsos

### Problema Actual
En [`AuditoriaController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/AuditoriaController.java) (líneas 31-36):
```java
@PostMapping
public ResponseEntity<Auditoria> guardar(@RequestBody Auditoria auditoria) {
    Auditoria nueva = auditoriaService.guardar(auditoria);
    return ResponseEntity.ok(nueva);
}
```
Cualquier cliente HTTP puede inyectar registros falsos de auditoría, destruyendo la integridad del registro de operaciones.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`AuditoriaController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/AuditoriaController.java) — Eliminar el endpoint `POST`. La auditoría SOLO debe generarse internamente a través de `AuditoriaListener`.

#### Cambios Específicos
Eliminar completamente el método `guardar()`:
```diff
- @PostMapping
- public ResponseEntity<Auditoria> guardar(@RequestBody Auditoria auditoria) {
-     Auditoria nueva = auditoriaService.guardar(auditoria);
-     return ResponseEntity.ok(nueva);
- }
```

Y agregar restricción por rol a los endpoints de lectura:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public ResponseEntity<List<Auditoria>> obtenerTodas() { ... }
```

#### Principio SOLID
- **SRP**: La auditoría es una consecuencia de las operaciones de negocio, no un recurso gestionable por el cliente. La responsabilidad de crear registros de auditoría pertenece exclusivamente a `AuditoriaListener`.
- **OCP**: Nuevas entidades auditables solo necesitan la anotación `@EntityListeners(AuditoriaListener.class)`, sin modificar el controlador.

### Criterio de Aceptación
- `POST /api/auditorias` → **405 Method Not Allowed** o **404 Not Found**.
- Los registros de auditoría se siguen generando automáticamente al crear/editar/eliminar entidades.
- `GET /api/auditorias` solo responde a usuarios con rol `ADMIN`.

---

## Tarea V6: Configurar CORS Restrictivo por Perfil de Entorno

### Problema Actual
En [`CorsConfig.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/config/CorsConfig.java) los orígenes permitidos están hardcodeados. Además, todos los controladores tienen `@CrossOrigin(origins = "*")` redundante, que sobreescribe la configuración centralizada.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`CorsConfig.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/config/CorsConfig.java) — Inyectar orígenes desde `application.properties`.
- **[MODIFY]** Todos los controladores — Eliminar las anotaciones `@CrossOrigin(origins = "*")` individuales. La política de CORS debe vivir centralizada en `CorsConfig`.
- **[MODIFY]** [`application.properties`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/resources/application.properties) — Agregar propiedad `cors.allowed-origins`.

#### Cambios Específicos

**`CorsConfig.java`**:
```java
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:8080,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        Arrays.stream(allowedOrigins).forEach(config::addAllowedOrigin);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Controladores** — Eliminar de todos:
```diff
- @CrossOrigin(origins = "*")
```

#### Principio SOLID
- **SRP**: La política de CORS vive en un único lugar (`CorsConfig`), no dispersa en cada controlador.
- **OCP**: Cambiar los orígenes permitidos solo requiere modificar una propiedad de configuración, sin tocar código Java.

### Criterio de Aceptación
- Solicitudes desde `http://localhost:8080` → permitidas.
- Solicitudes desde `http://malicious-site.com` → bloqueadas por CORS.
- Ningún controlador tiene `@CrossOrigin` a nivel de clase.

---

## Tarea V7: Agregar Validación de Entrada con Bean Validation en Endpoints de Autenticación

### Problema Actual
[`LoginRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/LoginRequest.java) y [`RegisterRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/RegisterRequest.java) no tienen anotaciones de validación (`@NotBlank`, `@Size`). Un cliente puede enviar un JSON vacío y la excepción será una NPE genérica en lugar de un mensaje de validación claro.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`LoginRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/LoginRequest.java) — Agregar `@NotBlank`.
- **[MODIFY]** [`RegisterRequest.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/dto/RegisterRequest.java) — Agregar `@NotBlank`, `@Size`.
- **[MODIFY]** [`AuthController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/security/AuthController.java) — Agregar `@Valid` a los parámetros `@RequestBody`.

#### Principio SOLID
- **SRP**: La validación de formato de los datos de entrada vive en los DTOs (anotaciones), no en la lógica del servicio o controlador.

### Criterio de Aceptación
- `POST /api/auth/login` con `{"username":"", "password":""}` → **400 Bad Request** con mensajes de validación claros.
- `POST /api/auth/register` con password de menos de 6 caracteres → **400 Bad Request** con mensaje descriptivo.

---

## Resumen de Archivos Impactados

| Archivo | Tarea(s) | Tipo |
| :--- | :--- | :--- |
| `security/SecurityConfig.java` | V1 | MODIFY |
| `dto/RegisterRequest.java` | V2, V7 | MODIFY |
| `security/AuthController.java` | V2, V7 | MODIFY |
| `dto/AdminCreateUserRequest.java` | V2 | NEW |
| `dto/UsuarioResponseDTO.java` | V3 | NEW |
| `dto/mapper/UsuarioMapper.java` | V3 | NEW |
| `controllers/UsuarioController.java` | V3 | MODIFY |
| `security/JwtUtil.java` | V4 | MODIFY |
| `application.properties` | V4, V6 | MODIFY |
| `controllers/AuditoriaController.java` | V5 | MODIFY |
| `config/CorsConfig.java` | V6 | MODIFY |
| Todos los controladores (`@CrossOrigin`) | V6 | MODIFY |
| `dto/LoginRequest.java` | V7 | MODIFY |

---

## Orden de Ejecución Sugerido

```
V1 (Security Config) → V4 (JWT Secret) → V2 (Registro) → V3 (DTOs Usuario) → V5 (Auditoría) → V6 (CORS) → V7 (Validación)
```

> Se empieza por V1 y V4 porque son los cimientos: si los endpoints no están protegidos ni el token es seguro, las demás mejoras no tienen sentido.
