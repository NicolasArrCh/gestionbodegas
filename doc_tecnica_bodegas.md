# 📚 Documentación Técnica - Sistema de Gestión de Bodegas

**Autores:** David Alejandro Ardila Cardozo, Nicolás Felipe Arrubla Chaux  
**Versión:** 1.0.0  
**Fecha:** Noviembre 2025

---

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Diagrama de Clases](#diagrama-de-clases)
4. [Modelo de Datos](#modelo-de-datos)
5. [Autenticación JWT](#autenticación-jwt)
6. [Sistema de Auditoría](#sistema-de-auditoría)
7. [API REST Endpoints](#api-rest-endpoints)
8. [Flujos de Negocio](#flujos-de-negocio)
9. [Seguridad](#seguridad)
10. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 1. Introducción

El **Sistema de Gestión de Bodegas** es una aplicación empresarial desarrollada con Spring Boot 3.5.7 que permite gestionar inventarios, movimientos de productos y control de acceso basado en roles (RBAC).

### Características Principales

- ✅ **Autenticación JWT** con roles (ADMIN, ENCARGADO, OPERADOR)
- ✅ **Gestión completa de inventarios** (productos, bodegas, movimientos)
- ✅ **Auditoría automática** de todas las operaciones CRUD
- ✅ **Validaciones de negocio** (stock, capacidad, permisos)
- ✅ **Registro de intentos fallidos** para análisis
- ✅ **API REST documentada** con Swagger/OpenAPI

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura en Capas

```
┌─────────────────────────────────────────────────────┐
│                  CAPA PRESENTACIÓN                  │
│  (HTML/CSS/JS - Dashboards por Rol + Login/Register)│
└────────────────────┬────────────────────────────────┘
                     │ HTTP/REST
┌────────────────────▼────────────────────────────────┐
│              CAPA DE CONTROLADORES                  │
│  (AuthController, BodegaController, ProductoController,│
│   MovimientoController, UsuarioController, etc.)    │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│       CAPA DE SEGURIDAD (Spring Security)           │
│  • JwtAuthenticationFilter                          │
│  • SecurityConfig (permisos por endpoint)           │
│  • JwtUtil (generación/validación tokens)           │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              CAPA DE SERVICIOS                      │
│  (BodegaService, ProductoService, UsuarioService,   │
│   MovimientoService, AuditoriaService, etc.)        │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│           CAPA DE PERSISTENCIA (JPA)                │
│  (Repositories: BodegaRepository, ProductoRepository,│
│   UsuarioRepository, MovimientoRepository, etc.)    │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              BASE DE DATOS (MySQL 8.0)              │
│  (Tablas: usuarios, bodegas, productos,             │
│   movimientos_inventario, auditoria, etc.)          │
└─────────────────────────────────────────────────────┘
```

### 2.2 Componentes Transversales

```
┌──────────────────────────────────────────────────┐
│         COMPONENTES TRANSVERSALES                │
├──────────────────────────────────────────────────┤
│  • AuditoriaListener (JPA Entity Listener)       │
│  • GlobalExceptionHandler (@ControllerAdvice)    │
│  • CorsConfig (configuración CORS)               │
│  • SwaggerConfig (documentación API)             │
│  • AuditorAwareImpl (auditoría JPA)              │
└──────────────────────────────────────────────────┘
```

---

## 3. Diagrama de Clases

### 3.1 Entidades Principales

```
┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│                    Usuario                          │
├─────────────────────────────────────────────────────┤
│ - id: Integer                                       │
│ - username: String (unique)                         │
│ - password: String (encriptado)                     │
│ - nombreCompleto: String                            │
│ - rol: Rol (ADMIN, ENCARGADO, OPERADOR)            │
├─────────────────────────────────────────────────────┤
│ + getRol(): Rol                                     │
│ + setPassword(String): void                         │
└─────────────────────────────────────────────────────┘
                    ▲
                    │ extends
                    │
┌───────────────────┴─────────────────────────────────┐
│              «MappedSuperclass»                     │
│                  Auditable                          │
├─────────────────────────────────────────────────────┤
│ - fechaCreacion: LocalDateTime                      │
│ - fechaModificacion: LocalDateTime                  │
│ - creadoPor: String                                 │
│ - modificadoPor: String                             │
└─────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│                    Bodega                           │
├─────────────────────────────────────────────────────┤
│ - id: Integer                                       │
│ - nombre: String                                    │
│ - ubicacion: String                                 │
│ - capacidad: Integer                                │
│ - encargado: Usuario                                │
├─────────────────────────────────────────────────────┤
│ + validarCapacidad(): boolean                       │
└─────────────────────────────────────────────────────┘
         │
         │ 1
         │
         │ *
         ▼
┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│                   Producto                          │
├─────────────────────────────────────────────────────┤
│ - id: Integer                                       │
│ - nombre: String                                    │
│ - categoria: String                                 │
│ - stock: Integer                                    │
│ - precio: BigDecimal                                │
│ - bodega: Bodega                                    │
├─────────────────────────────────────────────────────┤
│ + aumentarStock(Integer): void                      │
│ + disminuirStock(Integer): void                     │
└─────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│            MovimientoInventario                     │
├─────────────────────────────────────────────────────┤
│ - id: Integer                                       │
│ - fecha: LocalDateTime                              │
│ - tipo: TipoMovimiento (ENTRADA/SALIDA/TRANSFERENCIA)│
│ - usuario: Usuario                                  │
│ - bodegaOrigen: Bodega                              │
│ - bodegaDestino: Bodega                             │
├─────────────────────────────────────────────────────┤
│ + validarMovimiento(): boolean                      │
└─────────────────────────────────────────────────────┘
         │
         │ 1
         │
         │ *
         ▼
┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│              DetalleMovimiento                      │
├─────────────────────────────────────────────────────┤
│ - id: Integer                                       │
│ - movimiento: MovimientoInventario                  │
│ - producto: Producto                                │
│ - cantidad: Integer                                 │
├─────────────────────────────────────────────────────┤
│ + calcularTotal(): BigDecimal                       │
└─────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│                  Auditoria                          │
├─────────────────────────────────────────────────────┤
│ - id: Long                                          │
│ - fechaHora: LocalDateTime                          │
│ - tipoOperacion: TipoOperacion (INSERT/UPDATE/DELETE)│
│ - usuario: Usuario                                  │
│ - entidadAfectada: String                           │
│ - valorAnterior: String (JSON)                      │
│ - valorNuevo: String (JSON)                         │
└─────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────┐
│                    «Entity»                         │
│               IntentoFallido                        │
├─────────────────────────────────────────────────────┤
│ - id: Long                                          │
│ - fechaHora: LocalDateTime                          │
│ - tipoMovimiento: TipoMovimiento                    │
│ - razonError: String                                │
│ - usuario: Usuario                                  │
│ - producto: Producto                                │
│ - bodegaOrigen: Bodega                              │
│ - bodegaDestino: Bodega                             │
│ - cantidadIntentada: Integer                        │
│ - detallesAdicionales: String (JSON)                │
└─────────────────────────────────────────────────────┘
```

### 3.2 Relaciones Entre Entidades

```
Usuario ─────────┐
       │         │ 1
       │ 1       │
       │         ▼ *
       │      Bodega ──────┐
       │         │         │ 1
       │ 1       │ 1       │
       │         │         ▼ *
       │         │      Producto
       │         │         │ *
       │         │         │
       ▼ *       │         │
  MovimientoInventario     │
       │ 1                 │
       │                   │
       ▼ *                 │
  DetalleMovimiento ───────┘
       
       
Usuario ──────────┐
       │ 1        │ 1
       │          │
       ▼ *        ▼ *
   Auditoria  IntentoFallido
```

---

## 4. Modelo de Datos

### 4.1 Diagrama ER (Entity-Relationship)

```sql
┌─────────────────────┐
│     USUARIOS        │
├─────────────────────┤
│ PK id               │
│    username (UK)    │
│    password         │
│    nombre_completo  │
│    rol              │
│    fecha_creacion   │
│    fecha_modificacion│
│    creado_por       │
│    modificado_por   │
└─────────────────────┘
         │
         │ 1:N (encargado)
         ▼
┌─────────────────────┐       ┌─────────────────────┐
│     BODEGAS         │  1:N  │     PRODUCTOS       │
├─────────────────────┤◄──────┤─────────────────────┤
│ PK id               │       │ PK id               │
│    nombre           │       │    nombre           │
│    ubicacion        │       │    categoria        │
│    capacidad        │       │    stock            │
│ FK encargado_id     │       │    precio           │
│    fecha_creacion   │       │ FK bodega_id        │
│    ...              │       │    fecha_creacion   │
└─────────────────────┘       │    ...              │
         │                    └─────────────────────┘
         │                             │
         │ origen/destino              │
         ▼                             │
┌─────────────────────┐                │
│ MOVIMIENTOS_        │                │
│   INVENTARIO        │                │
├─────────────────────┤                │
│ PK id               │                │
│    fecha            │                │
│    tipo             │                │
│ FK usuario_id       │                │
│ FK bodega_origen_id │                │
│ FK bodega_destino_id│                │
│    fecha_creacion   │                │
│    ...              │                │
└─────────────────────┘                │
         │ 1:N                         │
         │                             │
         ▼                             │
┌─────────────────────┐                │
│ DETALLE_MOVIMIENTO  │◄───────────────┘
├─────────────────────┤
│ PK id               │
│ FK movimiento_id    │
│ FK producto_id      │
│    cantidad         │
│    fecha_creacion   │
│    ...              │
└─────────────────────┘


┌─────────────────────┐       ┌─────────────────────┐
│    AUDITORIA        │       │  INTENTOS_FALLIDOS  │
├─────────────────────┤       ├─────────────────────┤
│ PK id               │       │ PK id               │
│    fecha_hora       │       │    fecha_hora       │
│    tipo_operacion   │       │    tipo_movimiento  │
│ FK usuario_id       │       │    razon_error      │
│    entidad_afectada │       │ FK usuario_id       │
│    valor_anterior   │       │ FK producto_id      │
│    valor_nuevo      │       │ FK bodega_origen_id │
└─────────────────────┘       │ FK bodega_destino_id│
                              │    cantidad_intentada│
                              │    detalles_adicionales│
                              └─────────────────────┘
```

### 4.2 Claves y Restricciones

**Claves Primarias (PK)**
- Todas las tablas tienen un `id` autoincremental como PK

**Claves Foráneas (FK)**
- `bodegas.encargado_id` → `usuarios.id`
- `productos.bodega_id` → `bodegas.id`
- `movimientos_inventario.usuario_id` → `usuarios.id`
- `movimientos_inventario.bodega_origen_id` → `bodegas.id`
- `movimientos_inventario.bodega_destino_id` → `bodegas.id`
- `detalle_movimiento.movimiento_id` → `movimientos_inventario.id` (ON DELETE CASCADE)
- `detalle_movimiento.producto_id` → `productos.id`
- `auditoria.usuario_id` → `usuarios.id`
- `intentos_fallidos.usuario_id` → `usuarios.id`
- `intentos_fallidos.producto_id` → `productos.id`

**Restricciones Únicas (UK)**
- `usuarios.username` UNIQUE

**Enumeraciones**
- `usuarios.rol`: ADMIN, ENCARGADO, OPERADOR
- `movimientos_inventario.tipo`: ENTRADA, SALIDA, TRANSFERENCIA
- `auditoria.tipo_operacion`: INSERT, UPDATE, DELETE
- `intentos_fallidos.tipo_movimiento`: SALIDA, ENTRADA, TRANSFERENCIA

---

## 5. Autenticación JWT

### 5.1 Arquitectura de Seguridad

```
┌──────────────┐                    ┌──────────────┐
│   Cliente    │                    │   Servidor   │
│ (Frontend)   │                    │  (Backend)   │
└──────┬───────┘                    └──────┬───────┘
       │                                   │
       │ 1. POST /api/auth/login           │
       │    {username, password}           │
       ├──────────────────────────────────►│
       │                                   │ 2. Validar credenciales
       │                                   │    (Spring Security)
       │                                   │
       │                                   │ 3. Generar JWT
       │ 4. {token: "eyJhbGc..."}         │    (JwtUtil)
       │◄──────────────────────────────────┤
       │                                   │
       │ 5. Guardar token en localStorage  │
       │                                   │
       │ 6. GET /api/productos             │
       │    Header: Authorization: Bearer token
       ├──────────────────────────────────►│
       │                                   │ 7. Validar JWT
       │                                   │    (JwtAuthenticationFilter)
       │                                   │
       │                                   │ 8. Extraer usuario y rol
       │                                   │    (JwtUtil)
       │                                   │
       │                                   │ 9. Verificar permisos
       │                                   │    (SecurityConfig)
       │                                   │
       │ 10. Respuesta con datos           │
       │◄──────────────────────────────────┤
       │                                   │
```

### 5.2 Estructura del Token JWT

Un token JWT consta de tres partes separadas por puntos (`.`):

```
HEADER.PAYLOAD.SIGNATURE
```

#### Ejemplo de Token Real

```
eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJBRE1JTiIsInN1YiI6ImNnb21leiIsImlhdCI6MTczMjUwMDAwMCwiZXhwIjoxNzMyNTAzNjAwfQ.XYZ123abc...
```

#### Decodificación del Token

**HEADER** (decodificado de Base64):
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**PAYLOAD** (decodificado de Base64):
```json
{
  "rol": "ADMIN",
  "sub": "cgomez",
  "iat": 1732500000,
  "exp": 1732503600
}
```

**SIGNATURE**: Hash HMAC-SHA256 del header + payload con clave secreta

### 5.3 Campos del Token

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| `alg` | Algoritmo de firma | "HS256" |
| `typ` | Tipo de token | "JWT" |
| `sub` | Subject (username) | "cgomez" |
| `rol` | Rol del usuario | "ADMIN" |
| `iat` | Issued At (timestamp) | 1732500000 |
| `exp` | Expiration (timestamp) | 1732503600 |

### 5.4 Implementación en Código

#### Generación del Token (JwtUtil.java)

```java
public String generarToken(Usuario usuario) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", usuario.getRol().name());

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(usuario.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
}
```

#### Validación del Token (JwtAuthenticationFilter.java)

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                 HttpServletResponse response, 
                                 FilterChain filterChain) {
    String header = request.getHeader("Authorization");
    
    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);
        String username = jwtUtil.obtenerUsername(token);
        
        if (username != null && jwtUtil.validarToken(token, usuario)) {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    
    filterChain.doFilter(request, response);
}
```

### 5.5 Configuración de Seguridad

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/**").authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, 
                         UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

---

## 6. Sistema de Auditoría

### 6.1 Funcionamiento de la Auditoría

El sistema implementa auditoría automática mediante JPA Entity Listeners:

```
┌─────────────────────────────────────────────────────┐
│           FLUJO DE AUDITORÍA AUTOMÁTICA             │
└─────────────────────────────────────────────────────┘

1. Usuario realiza operación CRUD
         │
         ▼
2. JPA detecta cambio en entidad
         │
         ▼
3. AuditoriaListener intercepta evento
         │
         ├──► @PreUpdate  ──► Captura estado anterior
         ├──► @PostPersist ──► Registra INSERT
         ├──► @PostUpdate  ──► Registra UPDATE
         └──► @PostRemove  ──► Registra DELETE
         │
         ▼
4. Serializa entidad a JSON
         │
         ▼
5. Obtiene usuario actual (SecurityContext)
         │
         ▼
6. Crea registro en tabla auditoria
         │
         ▼
7. Guarda de forma asíncrona
   (AuditoriaAsyncService)
```

### 6.2 Ejemplo de Registro de Auditoría

**Operación UPDATE en Producto**

```json
{
  "id": 15,
  "fechaHora": "2025-11-16T14:30:00",
  "tipoOperacion": "UPDATE",
  "usuario": {
    "id": 1,
    "username": "cgomez"
  },
  "entidadAfectada": "Producto",
  "valorAnterior": "{\"id\":5,\"nombre\":\"Disco SSD 1TB\",\"stock\":40}",
  "valorNuevo": "{\"id\":5,\"nombre\":\"Disco SSD 1TB Samsung\",\"stock\":45}"
}
```

### 6.3 Sistema de Intentos Fallidos

Registra operaciones que no se completaron por validaciones de negocio:

```json
{
  "id": 3,
  "fechaHora": "2025-11-16T15:00:00",
  "tipoMovimiento": "SALIDA",
  "razonError": "Stock insuficiente en bodega origen: disponible=10",
  "usuario": {"id": 3},
  "producto": {"id": 2, "nombre": "Teclado Mecánico"},
  "bodegaOrigen": {"id": 1, "nombre": "Bodega Central"},
  "cantidadIntentada": 50,
  "detallesAdicionales": "{\"stockActual\":10,\"capacidadBodega\":5000}"
}
```

---

## 7. API REST Endpoints

### 7.1 Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Iniciar sesión | ❌ |
| POST | `/api/auth/register` | Registrar usuario | ❌ |

### 7.2 Usuarios

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/usuarios` | Listar usuarios | ADMIN |
| GET | `/api/usuarios/{id}` | Obtener usuario | ADMIN |
| POST | `/api/usuarios` | Crear usuario | ADMIN |
| PUT | `/api/usuarios/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario | ADMIN |
| GET | `/api/usuarios/username/{username}` | Buscar por username | ALL |
| GET | `/api/usuarios/rol/{rol}` | Listar por rol | ADMIN |

### 7.3 Bodegas

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/bodegas` | Listar bodegas | ALL |
| GET | `/api/bodegas/{id}` | Obtener bodega | ALL |
| POST | `/api/bodegas` | Crear bodega | ADMIN |
| PUT | `/api/bodegas/{id}` | Actualizar bodega | ADMIN |
| DELETE | `/api/bodegas/{id}` | Eliminar bodega | ADMIN |
| GET | `/api/bodegas/resumen-stock` | Stock por bodega | ALL |

### 7.4 Productos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/productos` | Listar productos | ALL |
| GET | `/api/productos/{id}` | Obtener producto | ALL |
| POST | `/api/productos` | Crear producto | ADMIN, ENCARGADO |
| PUT | `/api/productos/{id}` | Actualizar producto | ADMIN, ENCARGADO |
| DELETE | `/api/productos/{id}` | Eliminar producto | ADMIN, ENCARGADO |
| GET | `/api/productos/stock-bajo/{cantidad}` | Productos con stock bajo | ALL |
| GET | `/api/productos/mas-movidos` | Productos más movidos | ALL |

### 7.5 Movimientos de Inventario

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/movimientos` | Listar movimientos | ALL |
| GET | `/api/movimientos/{id}` | Obtener movimiento | ALL |
| POST | `/api/movimientos` | Crear movimiento | ADMIN, ENCARGADO |
| GET | `/api/movimientos/tipo/{tipo}` | Filtrar por tipo | ALL |
| GET | `/api/movimientos/usuario/{usuarioId}` | Por usuario | ALL |

### 7.6 Detalle de Movimientos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/detalle-movimientos` | Listar detalles | ALL |
| GET | `/api/detalle-movimientos/{id}` | Obtener detalle | ALL |
| POST | `/api/detalle-movimientos` | Crear detalle | ADMIN, ENCARGADO |
| GET | `/api/detalle-movimientos/movimiento/{id}` | Por movimiento | ALL |

### 7.7 Auditoría

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/auditorias` | Listar auditorías | ADMIN |
| GET | `/api/auditorias/tipo/{tipo}` | Por tipo operación | ADMIN |
| GET | `/api/auditorias/entidad/{entidad}` | Por entidad | ADMIN |
| GET | `/api/auditorias/fechas` | Por rango fechas | ADMIN |

### 7.8 Reportes

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/reportes/resumen-general` | Dashboard general | ALL |

---

## 8. Flujos de Negocio

### 8.1 Flujo de Login y Autenticación

```
┌──────────┐
│ Usuario  │
│ ingresa  │
│username/ │
│password  │
└────┬─────┘
     │
     ▼
┌─────────────────────┐
│ POST /auth/login    │
│ {username, password}│
└────┬────────────────┘
     │
     ▼
┌────────────────────────────┐
│ AuthenticationManager      │
│ valida credenciales        │
│ (Spring Security)          │
└────┬───────────────────────┘
     │
     ▼
┌────────────────────────────┐
│ CustomUserDetailsService   │
│ carga usuario de BD        │
│ y compara password         │
│ (BCrypt)                   │
└────┬───────────────────────┘
     │
     ├─── ❌ Credenciales inválidas
     │    └──► 401 Unauthorized
     │
     └─── ✅ Credenciales válidas
          │
          ▼
     ┌────────────────────┐
     │ JwtUtil genera     │
     │ token JWT con:     │
     │ - username (sub)   │
     │ - rol (claim)      │
     │ - exp (1 hora)     │
     └────┬───────────────┘
          │
          ▼
     ┌────────────────────┐
     │ Retorna            │
     │ {token: "eyJ..."}  │
     └────┬───────────────┘
          │
          ▼
     ┌────────────────────┐
     │ Cliente guarda     │
     │ token en           │
     │ localStorage       │
     └────────────────────┘
```

### 8.2 Flujo de Movimiento de Inventario (ENTRADA)

```
┌──────────────────────────────────────────────────────┐
│  1. Usuario crea Movimiento ENTRADA                 │
│     POST /api/movimientos                           │
│     {tipo: "ENTRADA", bodegaDestino: {id: 1}}      │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  2. Se guarda MovimientoInventario                  │
│     - fecha: NOW()                                  │
│     - tipo: ENTRADA                                 │
│     - usuario_id: (del token JWT)                   │
│     - bodega_destino_id: 1                          │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  3. Usuario agrega detalle                          │
│     POST /api/detalle-movimientos                   │
│     {movimiento: {id: X}, producto: {id: Y},        │
│      cantidad: 50}                                  │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  4. VALIDACIONES DE NEGOCIO                         │
│     ├─► Validar capacidad bodega destino           │
│     │   (stock_actual + cantidad <= capacidad)      │
│     └─► Si falla: lanzar excepción                 │
│         └──► Registrar en intentos_fallidos        │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  5. APLICAR CAMBIOS                                 │
│     ├─► Buscar producto en bodega destino          │
│     │   └─► Si existe: aumentar stock               │
│     │   └─► Si no existe: crear nuevo producto      │
│     └─► Guardar DetalleMovimiento                   │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  6. AUDITORÍA AUTOMÁTICA                            │
│     ├─► AuditoriaListener captura cambios          │
│     ├─► Serializa a JSON (anterior/nuevo)          │
│     └─► Guarda en tabla auditoria                   │
└──────────────────────────────────────────────────────┘
```

### 8.3 Flujo de Movimiento SALIDA

```
┌──────────────────────────────────────────────────────┐
│  1. Usuario crea Movimiento SALIDA                  │
│     {tipo: "SALIDA", bodegaOrigen: {id: 1}}        │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  2. Usuario agrega detalle con cantidad             │
│     {producto: {id: 5}, cantidad: 20}              │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  3. VALIDACIONES                                    │
│     ├─► Verificar que producto existe en bodega    │
│     │   origen                                      │
│     └─► Validar stock suficiente                   │
│         (stock_actual >= cantidad)                  │
│                                                     │
│     Si falla: ❌                                    │
│     └──► IllegalArgumentException                   │
│          └──► Registrar en intentos_fallidos       │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  4. APLICAR SALIDA                                  │
│     producto.stock = stock - cantidad               │
│     guardar(producto)                               │
└──────────────────────────────────────────────────────┘
```

### 8.4 Flujo de Movimiento TRANSFERENCIA

```
┌──────────────────────────────────────────────────────┐
│  1. Movimiento TRANSFERENCIA                        │
│     {tipo: "TRANSFERENCIA",                         │
│      bodegaOrigen: {id: 1},                        │
│      bodegaDestino: {id: 2}}                       │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  2. VALIDACIONES COMBINADAS                         │
│     ├─► Producto existe en bodega origen           │
│     ├─► Stock suficiente en origen                 │
│     └─► Capacidad disponible en destino            │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  3. APLICAR TRANSFERENCIA                           │
│     ├─► Disminuir stock en bodega origen           │
│     └─► Aumentar stock en bodega destino           │
│         (crear producto si no existe)               │
└──────────────────────────────────────────────────────┘
```

### 8.5 Diagrama de Secuencia - Crear Producto

```
Usuario    Controller    Service    Repository    AuditoriaListener    BD
  │            │            │            │                │             │
  │──POST──────►│            │            │                │             │
  │ /productos │            │            │                │             │
  │            │            │            │                │             │
  │            │──guardar──►│            │                │             │
  │            │  (producto)│            │                │             │
  │            │            │            │                │             │
  │            │            │─validar────┤                │             │
  │            │            │ capacidad  │                │             │
  │            │            │            │                │             │
  │            │            │──save──────►│                │             │
  │            │            │            │                │             │
  │            │            │            │──INSERT────────────────────► │
  │            │            │            │                │             │
  │            │            │            │◄───producto────────────────── │
  │            │            │            │   guardado                   │
  │            │            │            │                │             │
  │            │            │            │◄───@PostPersist─┤             │
  │            │            │            │                │             │
  │            │            │            │                │─capturar─►  │
  │            │            │            │                │  datos      │
  │            │            │            │                │             │
  │            │            │            │                │─obtener─►   │
  │            │            │            │                │ usuario     │
  │            │            │            │                │             │
  │            │            │            │                │─guardar─────►│
  │            │            │            │                │ auditoria   │
  │            │            │            │                │             │
  │            │            │◄──producto─┤                │             │
  │            │◄──producto─┤            │                │             │
  │◄──200 OK───┤            │            │                │             │
  │  producto  │            │            │                │             │
```

---

## 9. Seguridad

### 9.1 Capas de Seguridad

```
┌─────────────────────────────────────────────────────┐
│              CAPA 1: CORS                           │
│  • Orígenes permitidos configurados                 │
│  • Headers Authorization permitido                  │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│         CAPA 2: JWT AUTHENTICATION                  │
│  • JwtAuthenticationFilter                          │
│  • Validación de token en cada request              │
│  • Extracción de usuario y rol del token            │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│      CAPA 3: AUTHORIZATION (Roles)                  │
│  • SecurityConfig define permisos por endpoint      │
│  • Verificación de rol del usuario                  │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│     CAPA 4: VALIDACIÓN DE NEGOCIO                   │
│  • Services validan reglas de negocio               │
│  • Registro de intentos fallidos                    │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│          CAPA 5: AUDITORÍA                          │
│  • Registro de todas las operaciones                │
│  • Trazabilidad completa                            │
└─────────────────────────────────────────────────────┘
```

### 9.2 Encriptación de Contraseñas

**BCrypt con Salt Automático**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Al guardar usuario
public Usuario guardar(Usuario usuario) {
    if (usuario.getPassword() != null) {
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);
    }
    return usuarioRepository.save(usuario);
}
```

**Ejemplo de contraseña encriptada en BD:**
```
Contraseña original: admin123
Contraseña en BD: $2a$10$aoScDQEO.4uKepA6cBbkZugy26XEvT1Pa/fD1aemCyaO0h0QcWf0S
```

### 9.3 Matriz de Permisos por Rol

| Endpoint | ADMIN | ENCARGADO | OPERADOR |
|----------|-------|-----------|----------|
| `/api/auth/*` | ✅ | ✅ | ✅ |
| GET `/api/bodegas` | ✅ | ✅ | ✅ |
| POST `/api/bodegas` | ✅ | ❌ | ❌ |
| PUT/DELETE `/api/bodegas` | ✅ | ❌ | ❌ |
| GET `/api/productos` | ✅ | ✅ | ✅ |
| POST/PUT/DELETE `/api/productos` | ✅ | ✅ | ❌ |
| POST `/api/movimientos` | ✅ | ✅ | ❌ |
| GET `/api/movimientos` | ✅ | ✅ | ✅ |
| GET/POST `/api/usuarios` | ✅ | ❌ | ❌ |
| GET `/api/auditorias` | ✅ | ✅ | ❌ |
| GET `/api/reportes` | ✅ | ✅ | ✅ |

---

## 10. Ejemplos de Uso

### 10.1 Ejemplo Completo: Login y Crear Producto

#### Paso 1: Login

**Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "cgomez",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJBRE1JTiIsInN1YiI6ImNnb21leiIsImlhdCI6MTczMjUwMDAwMCwiZXhwIjoxNzMyNTAzNjAwfQ.signature"
}
```

#### Paso 2: Guardar Token

```javascript
localStorage.setItem('token', response.token);
```

#### Paso 3: Crear Producto

**Request:**
```http
POST http://localhost:8080/api/productos
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "nombre": "Mouse Inalámbrico Logitech",
  "categoria": "Periféricos",
  "stock": 35,
  "precio": 125000.00,
  "bodega": {
    "id": 1
  }
}
```

**Response:**
```json
{
  "id": 9,
  "nombre": "Mouse Inalámbrico Logitech",
  "categoria": "Periféricos",
  "stock": 35,
  "precio": 125000.00,
  "bodega": {
    "id": 1,
    "nombre": "Bodega Central"
  },
  "fechaCreacion": "2025-11-16T16:30:00",
  "creadoPor": "cgomez"
}
```

#### Paso 4: Verificar Auditoría

**Request:**
```http
GET http://localhost:8080/api/auditorias
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
[
  {
    "id": 25,
    "fechaHora": "2025-11-16T16:30:00",
    "tipoOperacion": "INSERT",
    "usuario": {
      "id": 1,
      "username": "cgomez"
    },
    "entidadAfectada": "Producto",
    "valorAnterior": null,
    "valorNuevo": "{\"id\":9,\"nombre\":\"Mouse Inalámbrico Logitech\",\"stock\":35}"
  }
]
```

### 10.2 Ejemplo: Movimiento de Salida con Validación

#### Paso 1: Crear Movimiento

**Request:**
```http
POST http://localhost:8080/api/movimientos
Authorization: Bearer token
Content-Type: application/json

{
  "tipo": "SALIDA",
  "usuario": {"id": 1},
  "bodegaOrigen": {"id": 1}
}
```

**Response:**
```json
{
  "id": 10,
  "fecha": "2025-11-16T17:00:00",
  "tipo": "SALIDA",
  "usuario": {"id": 1, "username": "cgomez"},
  "bodegaOrigen": {"id": 1, "nombre": "Bodega Central"}
}
```

#### Paso 2: Agregar Detalle (Caso exitoso)

**Request:**
```http
POST http://localhost:8080/api/detalle-movimientos
Authorization: Bearer token
Content-Type: application/json

{
  "movimiento": {"id": 10},
  "producto": {"id": 1},
  "cantidad": 5
}
```

**Response:**
```json
{
  "id": 15,
  "movimiento": {"id": 10},
  "producto": {
    "id": 1,
    "nombre": "Monitor LG 27\"",
    "stock": 30
  },
  "cantidad": 5
}
```

#### Paso 3: Intento con Stock Insuficiente

**Request:**
```http
POST http://localhost:8080/api/detalle-movimientos
Authorization: Bearer token
Content-Type: application/json

{
  "movimiento": {"id": 10},
  "producto": {"id": 1},
  "cantidad": 100
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-16T17:05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente en la bodega origen: disponible=30"
}
```

#### Paso 4: Verificar Intento Fallido

**Request:**
```http
GET http://localhost:8080/api/intentos-fallidos/ultimos
Authorization: Bearer token
```

**Response:**
```json
[
  {
    "id": 5,
    "fechaHora": "2025-11-16T17:05:00",
    "tipoMovimiento": "SALIDA",
    "razonError": "Stock insuficiente en la bodega origen: disponible=30",
    "usuario": {"id": 1, "username": "cgomez"},
    "producto": {"id": 1, "nombre": "Monitor LG 27\""},
    "bodegaOrigen": {"id": 1, "nombre": "Bodega Central"},
    "cantidadIntentada": 100
  }
]
```

### 10.3 Ejemplo Frontend: Consumo de API

```javascript
// Función para hacer llamadas autenticadas
async function apiCall(method, endpoint, body = null) {
    const token = localStorage.getItem('token');
    
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    
    if (body) {
        options.body = JSON.stringify(body);
    }
    
    const response = await fetch(`http://localhost:8080/api${endpoint}`, options);
    
    if (response.status === 401) {
        // Token expirado
        localStorage.removeItem('token');
        window.location.href = '/html/login.html';
        throw new Error('Token expirado');
    }
    
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
    }
    
    return await response.json();
}

// Ejemplo de uso
async function crearProducto() {
    try {
        const producto = {
            nombre: "Laptop Dell XPS",
            categoria: "Computadoras",
            stock: 10,
            precio: 4500000,
            bodega: { id: 1 }
        };
        
        const resultado = await apiCall('POST', '/productos', producto);
        console.log('Producto creado:', resultado);
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}
```

---

## 11. Conclusiones

### Fortalezas del Sistema

✅ **Seguridad Robusta**: JWT + BCrypt + RBAC  
✅ **Trazabilidad Completa**: Auditoría automática de todas las operaciones  
✅ **Validaciones de Negocio**: Control estricto de stock y capacidades  
✅ **Escalabilidad**: Arquitectura en capas bien definida  
✅ **Mantenibilidad**: Código limpio con Lombok y buenas prácticas  
✅ **Documentación**: Swagger UI para testing y documentación  

### Posibles Mejoras Futuras

- 🔄 Implementar caché con Redis para mejorar rendimiento
- 📊 Agregar más reportes y gráficas en el dashboard
- 📧 Sistema de notificaciones por email
- 🔍 Búsqueda avanzada con filtros complejos
- 📱 Aplicación móvil nativa
- 🌐 Internacionalización (i18n)
- 📦 Exportación de reportes a PDF/Excel
- 🔐 Autenticación de dos factores (2FA)

---

## 12. Glosario Técnico

| Término | Definición |
|---------|------------|
| **JWT** | JSON Web Token - Token de autenticación estándar |
| **RBAC** | Role-Based Access Control - Control de acceso basado en roles |
| **CRUD** | Create, Read, Update, Delete - Operaciones básicas de BD |
| **JPA** | Java Persistence API - Estándar de persistencia Java |
| **BCrypt** | Algoritmo de hash seguro para contraseñas |
| **DTO** | Data Transfer Object - Objeto de transferencia de datos |
| **Entity Listener** | Listener de eventos JPA para auditoría automática |
| **CORS** | Cross-Origin Resource Sharing - Política de recursos compartidos |
| **REST** | Representational State Transfer - Arquitectura de servicios web |
| **ORM** | Object-Relational Mapping - Mapeo objeto-relacional |

---

## 13. Referencias

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Spring Security JWT**: https://spring.io/guides/tutorials/spring-boot-oauth2/
- **JWT.io**: https://jwt.io/
- **Swagger/OpenAPI**: https://swagger.io/specification/
- **MySQL Documentation**: https://dev.mysql.com/doc/

--- 

**Documentación actualizada:** Noviembre 2025  
**Versión del sistema:** 1.0.0  

---

*Este documento ha sido generado para facilitar el entendimiento técnico del Sistema de Gestión de Bodegas. Para más información sobre implementación y despliegue, consultar el archivo README.md en el repositorio.*