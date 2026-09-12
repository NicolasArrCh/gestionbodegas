# 🟠 Plan de Implementación 2: Deuda Técnica y Calidad de Código

**Proyecto:** Sistema de Gestión de Bodegas  
**Prioridad:** Alta (se ejecuta DESPUÉS del Plan de Vulnerabilidades)  
**Principios Rectores:** SOLID + Modularización estricta  
**Prerequisito:** Plan 1 (Vulnerabilidades Críticas) completado o al menos tareas V1-V4.

---

## Principios SOLID Aplicados en este Plan

| Principio | Aplicación Concreta |
| :--- | :--- |
| **S – Single Responsibility** | Cada Service tiene una única razón de cambio. Se separa la lógica de validación de inventario, la orquestación de movimientos y el mapeo de DTOs en clases independientes. |
| **O – Open/Closed** | Los mappers y validadores se pueden extender (nuevos tipos de movimiento, nuevos DTOs) sin modificar las clases existentes. |
| **L – Liskov Substitution** | Las interfaces de repositorio y servicio permiten sustituir implementaciones (test con H2 vs producción con MySQL) sin cambiar la lógica. |
| **I – Interface Segregation** | Se definen contratos de servicio específicos por dominio en lugar de un servicio monolítico. |
| **D – Dependency Inversion** | Los controladores y servicios dependen de interfaces/abstracciones, no de clases concretas. Se usa inyección por constructor en lugar de `@Autowired` en campos. |

---

## Tarea DT1: Corregir Bug de Doble Encriptación de Contraseñas

### Problema Actual
En [`UsuarioService.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/services/UsuarioService.java) (líneas 33-41):
```java
public Usuario guardar(Usuario usuario) {
    if (usuario.getPassword() != null) {
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);
    }
    return usuarioRepository.save(usuario);
}
```
El método `guardar()` se usa tanto para **crear** como para **actualizar** usuarios. Si se actualiza el nombre de un usuario existente sin cambiar la contraseña, el hash BCrypt que ya estaba guardado se vuelve a encriptar (hash de un hash). El usuario nunca más podrá iniciar sesión.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`UsuarioService.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/services/UsuarioService.java) — Separar métodos `crear()` y `actualizar()`.

#### Cambios Específicos
```java
/**
 * Crea un nuevo usuario. Encripta la contraseña proporcionada.
 */
public Usuario crear(Usuario usuario) {
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    return usuarioRepository.save(usuario);
}

/**
 * Actualiza un usuario existente. Solo encripta la contraseña
 * si se proporcionó una nueva (diferente a la almacenada).
 */
public Usuario actualizar(Integer id, Usuario datosNuevos) {
    Usuario existente = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

    existente.setNombreCompleto(datosNuevos.getNombreCompleto());
    existente.setRol(datosNuevos.getRol());

    // Solo re-encriptar si se envió una contraseña nueva (no vacía y diferente al hash actual)
    if (datosNuevos.getPassword() != null
            && !datosNuevos.getPassword().isBlank()
            && !datosNuevos.getPassword().startsWith("$2a$")) {
        existente.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
    }
    // Si no se envía password o viene el hash, se mantiene la contraseña actual

    return usuarioRepository.save(existente);
}
```

#### Principio SOLID
- **SRP**: Dos métodos con responsabilidades claras — `crear()` siempre encripta, `actualizar()` solo cuando corresponde.
- **OCP**: Si mañana se necesita un tercer flujo (ej. reset de contraseña), se agrega un nuevo método sin modificar los existentes.

### Criterio de Aceptación
- Crear usuario → la contraseña se guarda encriptada → puede hacer login.
- Actualizar solo el `nombreCompleto` de un usuario existente → la contraseña **no** cambia → el usuario puede seguir haciendo login con su contraseña original.
- Actualizar con nueva contraseña en texto plano → se encripta correctamente → puede hacer login con la nueva contraseña.

---

## Tarea DT2: Unificar Movimientos de Inventario en una Operación Atómica

### Problema Actual
El frontend (por ejemplo en [`dashboard.js`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/resources/js/dashboard.js) líneas ~549-569) ejecuta **dos llamadas HTTP separadas** para crear un movimiento:

1. `POST /api/movimientos` → crea la cabecera del movimiento
2. `POST /api/detalle-movimientos` → crea el detalle y aplica validaciones de stock

Si la segunda llamada falla (ej. stock insuficiente), la cabecera del movimiento queda huérfana en la base de datos y el inventario queda inconsistente.

### Solución Propuesta

#### Archivos Nuevos y Modificados
- **[NEW]** `dto/MovimientoRequestDTO.java` — DTO unificado que recibe cabecera + lista de detalles en un solo payload.
- **[NEW]** `dto/MovimientoDetalleDTO.java` — DTO para cada línea de detalle del movimiento.
- **[NEW]** `dto/MovimientoResponseDTO.java` — DTO de respuesta con el movimiento completo.
- **[NEW]** `dto/mapper/MovimientoMapper.java` — Mapper para convertir entre DTOs y entidades.
- **[NEW]** `services/MovimientoOrquestadorService.java` — Servicio orquestador que ejecuta la operación completa en una sola transacción `@Transactional`.
- **[MODIFY]** [`MovimientoInventarioController.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/MovimientoInventarioController.java) — Nuevo endpoint unificado.
- **[MODIFY]** Frontend JS (los 3 archivos dashboard) — Llamar al nuevo endpoint unificado.

#### Cambios Específicos

**`MovimientoRequestDTO.java`** (NUEVO):
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class MovimientoRequestDTO {
    @NotNull private MovimientoInventario.TipoMovimiento tipo;
    private Integer bodegaOrigenId;
    private Integer bodegaDestinoId;

    @NotEmpty(message = "Debe incluir al menos un detalle de movimiento")
    @Valid
    private List<MovimientoDetalleDTO> detalles;
}
```

**`MovimientoDetalleDTO.java`** (NUEVO):
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class MovimientoDetalleDTO {
    @NotNull private Integer productoId;
    @NotNull @Min(1) private Integer cantidad;
}
```

**`MovimientoOrquestadorService.java`** (NUEVO) — Clase orquestadora:
```java
@Service
public class MovimientoOrquestadorService {

    // Inyección por constructor (DIP)
    private final MovimientoInventarioRepository movimientoRepo;
    private final DetalleMovimientoRepository detalleRepo;
    private final ProductoRepository productoRepo;
    private final BodegaRepository bodegaRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioValidadorService validador;

    // Constructor con todos los parámetros...

    @Transactional(rollbackFor = Exception.class)
    public MovimientoInventario ejecutarMovimiento(MovimientoRequestDTO request, String username) {
        // 1. Resolver usuario autenticado
        // 2. Resolver bodegas origen/destino
        // 3. VALIDAR TODAS las líneas ANTES de aplicar cambios
        // 4. Crear cabecera del movimiento
        // 5. Crear detalles y aplicar cambios de stock
        // 6. Si cualquier paso falla → rollback automático de TODO
    }
}
```

**Nuevo endpoint en `MovimientoInventarioController.java`**:
```java
@PostMapping("/ejecutar")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO')")
public ResponseEntity<MovimientoResponseDTO> ejecutarMovimiento(
        @Valid @RequestBody MovimientoRequestDTO request,
        Authentication authentication) {
    MovimientoInventario resultado = orquestadorService.ejecutarMovimiento(
            request, authentication.getName());
    return ResponseEntity.ok(movimientoMapper.toResponseDTO(resultado));
}
```

#### Principio SOLID
- **SRP**: `MovimientoOrquestadorService` tiene una sola responsabilidad: coordinar la ejecución atómica de un movimiento completo. La validación se delega a `InventarioValidadorService` (un validador separado).
- **DIP**: El orquestador depende de interfaces de repositorios, no de implementaciones concretas.
- **OCP**: Para agregar un nuevo tipo de movimiento (ej. `AJUSTE`), se extiende el validador sin modificar el orquestador.

### Criterio de Aceptación
- `POST /api/movimientos/ejecutar` con un payload completo → crea cabecera + detalles + ajusta stock, todo en una transacción.
- Si una validación de stock falla en el detalle #3 de 5 → **ningún** detalle se persiste, **ningún** stock se modifica y no queda cabecera huérfana.
- El endpoint antiguo `POST /api/movimientos` se marca como `@Deprecated` para no romper compatibilidad inmediata.

---

## Tarea DT3: Adoptar DTOs Completos para Todas las Entidades del Dominio

### Problema Actual
Actualmente los controladores de [`BodegaController`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/BodegaController.java), [`ProductoController`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/ProductoController.java) y [`DetalleMovimientoController`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/controllers/DetalleMovimientoController.java) reciben y retornan entidades JPA directamente. Esto genera:
- **Mass Assignment**: Un cliente puede enviar campos que no debería (ej. `id`, `fechaCreacion`, `creadoPor`).
- **Acoplamiento**: El contrato de la API está acoplado al modelo de persistencia. Cambiar una columna en la BD rompe la API.
- **Recursión infinita**: Relaciones bidireccionales JPA pueden causar `StackOverflowError` al serializar a JSON.

### Solución Propuesta

#### Archivos Nuevos
Crear un paquete `dto/` organizado por dominio:

```
dto/
├── bodega/
│   ├── BodegaRequestDTO.java
│   ├── BodegaResponseDTO.java
│   └── BodegaResumenDTO.java
├── producto/
│   ├── ProductoRequestDTO.java
│   └── ProductoResponseDTO.java
├── movimiento/
│   ├── MovimientoRequestDTO.java      (de tarea DT2)
│   ├── MovimientoDetalleDTO.java      (de tarea DT2)
│   └── MovimientoResponseDTO.java     (de tarea DT2)
├── mapper/
│   ├── BodegaMapper.java
│   ├── ProductoMapper.java
│   ├── MovimientoMapper.java
│   └── UsuarioMapper.java            (de tarea V3)
├── LoginRequest.java                  (existente)
├── LoginResponse.java                 (existente)
├── RegisterRequest.java               (existente, modificado en V2)
├── AdminCreateUserRequest.java        (de tarea V2)
└── UsuarioResponseDTO.java            (de tarea V3)
```

#### Patrón de Cada DTO

**Request DTO** (lo que recibe la API):
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class BodegaRequestDTO {
    @NotBlank private String nombre;
    @NotBlank private String ubicacion;
    @Min(1) private Integer capacidad;
    @NotNull private Integer encargadoId;  // Solo el ID, no la entidad completa
}
```

**Response DTO** (lo que devuelve la API):
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BodegaResponseDTO {
    private Integer id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private String encargadoNombre;  // Solo el nombre, no la entidad completa
    private LocalDateTime fechaCreacion;
}
```

**Mapper** (conversión entre entidad y DTO):
```java
@Component
public class BodegaMapper {
    public BodegaResponseDTO toDTO(Bodega bodega) { ... }
    public List<BodegaResponseDTO> toDTOList(List<Bodega> bodegas) { ... }
    // No tiene toEntity() → la creación de entidades desde DTOs se hace en el servicio
}
```

#### Principio SOLID
- **SRP**: Cada Mapper tiene una sola responsabilidad: transformar entre entidad y DTO de un dominio específico.
- **ISP**: El cliente solo ve los campos relevantes para su operación (Request DTO vs Response DTO).
- **DIP**: Los controladores dependen de los DTOs (contratos públicos), no de las entidades JPA (detalles internos).

### Criterio de Aceptación
- Ningún controlador recibe ni retorna una entidad JPA directamente.
- Los Response DTOs no contienen campos internos como `password`, `creadoPor`, `modificadoPor`.
- Los Request DTOs usan `@Valid` con anotaciones de Bean Validation para garantizar datos correctos.

---

## Tarea DT4: Migrar de @Autowired en Campos a Inyección por Constructor

### Problema Actual
Todos los servicios y controladores usan `@Autowired` en campos privados:
```java
@Autowired
private ProductoService productoService;
```
Esto dificulta las pruebas unitarias (no se pueden inyectar mocks sin reflection), viola DIP al ocultar dependencias y hace que las clases sean mutables después de la construcción.

### Solución Propuesta

#### Archivos a Modificar
Todos los archivos en `controllers/`, `services/`, `config/`, `security/` y `listeners/` que usen `@Autowired` en campos.

#### Patrón a Aplicar
Reemplazar `@Autowired` de campo por inyección por constructor con `final`:

**Antes:**
```java
@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private BodegaRepository bodegaRepository;
}
```

**Después:**
```java
@Service
@RequiredArgsConstructor  // Lombok genera el constructor automáticamente
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
}
```

#### Principio SOLID
- **DIP**: Las dependencias se declaran explícitamente en el constructor. Las clases dependen de abstracciones inyectadas, no de detalles resueltos por reflection.
- **SRP**: Al hacer las dependencias explícitas con `final`, se vuelve visible cuando una clase tiene demasiadas dependencias (señal de que viola SRP).

### Criterio de Aceptación
- Cero usos de `@Autowired` en campos en todo el proyecto (excepto en `AuditoriaListener` que requiere inyección estática por limitación de JPA).
- Todas las dependencias son `private final`.
- El proyecto compila y funciona sin cambios de comportamiento.

---

## Tarea DT5: Reemplazar System.out.println por SLF4J Logger

### Problema Actual
Todo el proyecto usa `System.out.println()` y `System.err.println()` para logging:
```java
System.out.println("✅ Token válido para usuario: " + username);
System.out.println("❌ Error procesando token: " + e.getMessage());
System.err.println("⚠️ Error obteniendo auditor actual: " + e.getMessage());
```
Esto no tiene niveles de severidad, no se puede filtrar por paquete y no es configurable en producción.

### Solución Propuesta

#### Archivos a Modificar
Todos los archivos que contengan `System.out.println` o `System.err.println`.

#### Patrón a Aplicar
```java
// Antes
System.out.println("✅ Token válido para usuario: " + username);
System.err.println("❌ Error: " + e.getMessage());

// Después
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ...
    log.debug("Token válido para usuario: {}", username);
    log.error("Error procesando token: {}", e.getMessage(), e);
}
```

#### Principio SOLID
- **SRP**: El logging es una preocupación transversal que debe delegarse a un framework dedicado (SLF4J/Logback), no gestionarse manualmente con `System.out`.

### Criterio de Aceptación
- Cero `System.out.println` o `System.err.println` en todo el proyecto.
- Los niveles de log se usan correctamente: `log.debug()` para trazas de desarrollo, `log.info()` para eventos de negocio, `log.warn()` para situaciones recuperables, `log.error()` para errores.
- Se puede configurar el nivel de log por paquete en `application.properties`.

---

## Tarea DT6: Corregir Warnings de Compilación de Lombok

### Problema Actual
La compilación produce 6 warnings de Lombok:
1. `@Builder will ignore the initializing expression` en [`Usuario.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Usuario.java) — el valor `Rol.OPERADOR` por defecto se ignora cuando se usa `@Builder`.
2. `Generating equals/hashCode without superclass call` en [`DetalleMovimiento`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/DetalleMovimiento.java), [`MovimientoInventario`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/MovimientoInventario.java), [`Usuario`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Usuario.java), [`Producto`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Producto.java), [`Bodega`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Bodega.java) — las entidades extienden `Auditable` pero `@Data` genera `equals/hashCode` sin llamar a `super`.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`Usuario.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/entities/Usuario.java) — Agregar `@Builder.Default` al campo `rol`.
- **[MODIFY]** Todas las entidades que extienden `Auditable` — Agregar `@EqualsAndHashCode(callSuper = false)`.

#### Cambios Específicos

**`Usuario.java`**:
```java
@Builder.Default
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private Rol rol = Rol.OPERADOR;
```

**Todas las entidades** (Usuario, Bodega, Producto, MovimientoInventario, DetalleMovimiento):
```java
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "...")
public class ... extends Auditable { ... }
```

#### Principio SOLID
- **LSP**: Las entidades que extienden `Auditable` deben funcionar correctamente como subclases, incluyendo un contrato coherente de `equals/hashCode`.

### Criterio de Aceptación
- `.\mvnw.cmd test-compile` produce **cero warnings** de Lombok.

---

## Tarea DT7: Reemplazar API Deprecada TransactionSynchronizationAdapter

### Problema Actual
En [`AuditoriaListener.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/listeners/AuditoriaListener.java) se usa `TransactionSynchronizationAdapter` que está **deprecada** desde Spring 5.3.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** [`AuditoriaListener.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/listeners/AuditoriaListener.java) — Reemplazar `TransactionSynchronizationAdapter` por `TransactionSynchronization` (interfaz con métodos default en Java 8+).

#### Cambios Específicos
```java
// Antes
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
new TransactionSynchronizationAdapter() {
    @Override
    public void afterCommit() { ... }
}

// Después
import org.springframework.transaction.support.TransactionSynchronization;
new TransactionSynchronization() {
    @Override
    public void afterCommit() { ... }
}
```

#### Principio SOLID
- **LSP**: `TransactionSynchronization` es la interfaz padre; usar directamente la interfaz en lugar de la clase adapter deprecada respeta la sustitución de Liskov.

### Criterio de Aceptación
- Cero uso de `TransactionSynchronizationAdapter` en el proyecto.
- La auditoría sigue funcionando correctamente después del cambio.
- Cero warnings de "deprecated API" en la compilación.

---

## Tarea DT8: Eliminar Duplicación de `obtenerUsuarioActual()` y `obtenerUsuarioSistema()`

### Problema Actual
El método `obtenerUsuarioActual()` está copiado y pegado en **tres clases distintas**:
- [`AuditoriaListener.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/listeners/AuditoriaListener.java) (líneas 182-197)
- [`AuditoriaErrorService.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/services/AuditoriaErrorService.java) (líneas 120-136)
- [`DetalleMovimientoService.java`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/java/com/c3/gestionbodegas/services/DetalleMovimientoService.java) (líneas 125-144)

Lo mismo ocurre con `obtenerUsuarioSistema()`. Esto viola DRY y SRP.

### Solución Propuesta

#### Archivos Nuevos y Modificados
- **[NEW]** `services/SecurityContextService.java` — Servicio único y reutilizable para resolver el usuario autenticado actual.
- **[MODIFY]** `AuditoriaListener.java`, `AuditoriaErrorService.java`, `DetalleMovimientoService.java` — Reemplazar los métodos duplicados por una llamada a `SecurityContextService`.

#### Cambios Específicos

**`SecurityContextService.java`** (NUEVO):
```java
@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario autenticado del SecurityContext.
     * Si no hay usuario autenticado, retorna el usuario "sistema" (ID 1).
     */
    public Usuario obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return usuarioRepository.findByUsername(auth.getName())
                        .orElseGet(this::obtenerUsuarioSistema);
            }
        } catch (Exception e) {
            log.warn("Error obteniendo usuario actual: {}", e.getMessage());
        }
        return obtenerUsuarioSistema();
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findById(1).orElseGet(() -> {
            Usuario temp = new Usuario();
            temp.setId(1);
            temp.setUsername("sistema");
            return temp;
        });
    }
}
```

#### Principio SOLID
- **SRP**: La resolución del usuario autenticado es una sola responsabilidad, centralizada en un único servicio.
- **DRY**: Se elimina la triplicación de código. Un solo punto de mantenimiento.

### Criterio de Aceptación
- El método `obtenerUsuarioActual()` solo existe en `SecurityContextService`.
- Las tres clases que lo usaban ahora delegan a `SecurityContextService`.
- El proyecto compila y la auditoría sigue registrando correctamente el usuario responsable.

---

## Tarea DT9: Agregar Paginación en Endpoints de Listado Masivo

### Problema Actual
Los endpoints `GET /api/productos`, `GET /api/bodegas`, `GET /api/movimientos`, `GET /api/auditorias` devuelven **todas** las filas de la tabla sin paginación. En producción con miles de registros, esto puede generar timeouts y uso excesivo de memoria.

### Solución Propuesta

#### Archivos a Modificar
- **[MODIFY]** Los Repositories — ya extienden `JpaRepository` que incluye `PagingAndSortingRepository`.
- **[MODIFY]** Los Services — agregar métodos con parámetro `Pageable`.
- **[MODIFY]** Los Controllers — aceptar parámetros de query `?page=0&size=20&sort=id,desc`.

#### Patrón a Aplicar

**Controller**:
```java
@GetMapping
public ResponseEntity<Page<ProductoResponseDTO>> obtenerTodos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    Page<Producto> productos = productoService.obtenerTodosPaginado(pageable);
    Page<ProductoResponseDTO> dtos = productos.map(productoMapper::toDTO);
    return ResponseEntity.ok(dtos);
}
```

> **Nota:** Los endpoints existentes sin paginación se mantienen temporalmente como `@Deprecated` para no romper el frontend. Se eliminan cuando el frontend migre a la versión paginada.

#### Principio SOLID
- **OCP**: La paginación se agrega como extensión. Los métodos sin paginar siguen existiendo como `@Deprecated`.
- **ISP**: El cliente que no necesita paginación puede seguir usando el endpoint sin parámetros (valores por defecto).

### Criterio de Aceptación
- `GET /api/productos?page=0&size=5` → devuelve solo 5 productos con metadatos de paginación (`totalElements`, `totalPages`, `number`).
- `GET /api/productos` sin parámetros → devuelve los primeros 20 registros por defecto.

---

## Tarea DT10: Eliminar Dependencia de MySQL y Migrar a H2 Local + Preparar Supabase

### Problema Actual
El proyecto **requiere MySQL 8 instalado y configurado localmente** para poder arrancar. **No se desea instalar MySQL ni ninguna base de datos local.** Esto dificulta:
- La ejecución inmediata del proyecto con un solo comando.
- La ejecución de pruebas automáticas en CI/CD.
- El onboarding de nuevos desarrolladores.
- La evaluación rápida por reclutadores que revisan portafolios.

### Decisión de Arquitectura
> **⚠️ IMPORTANTE:** MySQL se elimina completamente del flujo local de desarrollo. La estrategia de base de datos queda así:
> - **Desarrollo local (por defecto):** H2 en memoria — sin instalar nada, arranca con un solo comando.
> - **Producción (futuro):** Supabase (PostgreSQL en la nube) — se implementará como perfil `prod` cuando se validen las funcionalidades actuales.
>
> La migración futura a Supabase es viable porque Spring Data JPA abstrae el motor de base de datos. El código Java no cambia; solo cambia la configuración del `DataSource` y el dialecto.

### Solución Propuesta

#### Archivos Nuevos, Modificados y Eliminados
- **[MODIFY]** [`pom.xml`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/pom.xml) — Reemplazar la dependencia de MySQL por H2 (principal) y agregar PostgreSQL (preparación para Supabase).
- **[MODIFY]** [`application.properties`](file:///c:/Users/nicolas.arrubla/Documents/Externo/gestionbodegas/src/main/resources/application.properties) — Configurar H2 como base de datos por defecto. Eliminar la configuración de MySQL.
- **[NEW]** `src/main/resources/application-prod.properties` — Perfil de producción preparado para Supabase (PostgreSQL), desactivado por defecto.
- **[MODIFY]** `src/main/resources/data.sql` — Adaptar sentencias SQL para compatibilidad H2 (ej. el tipo `ENUM` de MySQL no existe en H2, usar `VARCHAR` con `@Enumerated(EnumType.STRING)` que ya está presente en las entidades).
- **[DELETE]** `src/main/resources/schema.sql` — Ya no se necesita porque `spring.jpa.hibernate.ddl-auto=create-drop` genera el schema automáticamente desde las entidades JPA.

#### Cambios Específicos

**`pom.xml`** — Reemplazar MySQL por H2 y agregar PostgreSQL (preparación Supabase):
```xml
<!-- ❌ ELIMINAR dependencia de MySQL -->
<!-- <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency> -->

<!-- ✅ H2 para desarrollo local (base de datos por defecto) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- ✅ PostgreSQL para producción futura con Supabase -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**`application.properties`** — H2 como BD principal (sin MySQL):
```properties
spring.application.name=gestionbodegas
server.port=8080

# ── Base de Datos: H2 en memoria (desarrollo local, sin instalar nada) ──
spring.datasource.url=jdbc:h2:mem:gestion_bodegas;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Consola H2 (accesible en http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Cargar datos de prueba automáticamente
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# ── JWT ──
jwt.secret=${JWT_SECRET:CambiarEstoEnProduccion_ClaveSegura2026}
jwt.expiration-minutes=${JWT_EXPIRATION:60}

# ── Swagger/OpenAPI ──
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs

# ── Logging ──
logging.level.root=INFO
logging.level.org.springframework.security=INFO

# ── Jackson ──
spring.jackson.time-zone=America/Bogota
spring.jackson.locale=es_CO

# ── Perfil activo (por defecto: local con H2) ──
spring.profiles.active=${SPRING_PROFILES_ACTIVE:default}
```

**`application-prod.properties`** (NUEVO — Supabase/PostgreSQL, activar cuando se migre):
```properties
# ══════════════════════════════════════════════════════════
# PERFIL DE PRODUCCIÓN: Supabase (PostgreSQL en la nube)
# Activar con: -Dspring.profiles.active=prod
# o con variable de entorno: SPRING_PROFILES_ACTIVE=prod
# ══════════════════════════════════════════════════════════

spring.datasource.url=${SUPABASE_DB_URL}
spring.datasource.username=${SUPABASE_DB_USER}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Desactivar consola H2 en producción
spring.h2.console.enabled=false

# No cargar data.sql en producción
spring.sql.init.mode=never
```

**`data.sql`** — Adaptar para compatibilidad H2:
- Eliminar las sentencias `ALTER TABLE ... ADD COLUMN` (ya no necesarias, las columnas vienen de `Auditable`).
- Eliminar las sentencias `UPDATE ... SET fecha_creacion = NOW()` (se populan automáticamente por JPA Auditing).
- Asegurar que los `INSERT` usen sintaxis estándar SQL (H2 con `MODE=PostgreSQL` acepta la mayoría).

#### Principio SOLID
- **DIP**: La aplicación depende de la abstracción `DataSource` de Spring, no de la implementación concreta de MySQL ni de ningún motor específico. Cambiar de H2 a PostgreSQL/Supabase es un cambio de configuración (`application-prod.properties`), no de código Java.
- **OCP**: Agregar un nuevo perfil (ej. `staging` con otra BD) solo requiere un nuevo archivo `.properties` sin modificar ninguna clase Java.
- **LSP**: Gracias a que JPA abstrae la capa de persistencia, H2, MySQL y PostgreSQL son intercambiables como implementaciones del mismo contrato `DataSource`.

### Criterio de Aceptación
- `.\mvnw.cmd spring-boot:run` **sin ninguna base de datos instalada** → la aplicación arranca con H2 en memoria y carga los datos de prueba.
- La consola H2 está accesible en `http://localhost:8080/h2-console`.
- El frontend funciona normalmente contra H2.
- El `pom.xml` ya no contiene la dependencia `mysql-connector-j`.
- El perfil `application-prod.properties` está listo para conectar a Supabase con solo definir las variables de entorno `SUPABASE_DB_URL`, `SUPABASE_DB_USER` y `SUPABASE_DB_PASSWORD`.
- Al ejecutar con `-Dspring.profiles.active=prod` y las variables de Supabase configuradas → la aplicación conecta a PostgreSQL en la nube.

### Nota sobre la Migración Futura a Supabase
> Cuando se decida migrar a Supabase:
> 1. Crear proyecto en [supabase.com](https://supabase.com) y obtener las credenciales de conexión PostgreSQL.
> 2. Definir las variables de entorno (`SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`).
> 3. Ejecutar con `SPRING_PROFILES_ACTIVE=prod` → Spring Boot conecta automáticamente a Supabase.
> 4. La primera ejecución con `ddl-auto=update` creará las tablas automáticamente en Supabase.
> 5. Considerar migrar `data.sql` a un script de seed de producción o a la interfaz de Supabase.
>
> **No se requieren cambios en el código Java** gracias a la abstracción de JPA (principio DIP).

---

## Resumen de Archivos Impactados

| Archivo | Tarea(s) | Tipo |
| :--- | :--- | :--- |
| `services/UsuarioService.java` | DT1 | MODIFY |
| `dto/MovimientoRequestDTO.java` | DT2 | NEW |
| `dto/MovimientoDetalleDTO.java` | DT2 | NEW |
| `dto/MovimientoResponseDTO.java` | DT2 | NEW |
| `dto/mapper/MovimientoMapper.java` | DT2 | NEW |
| `services/MovimientoOrquestadorService.java` | DT2 | NEW |
| `controllers/MovimientoInventarioController.java` | DT2 | MODIFY |
| `dto/bodega/BodegaRequestDTO.java` | DT3 | NEW |
| `dto/bodega/BodegaResponseDTO.java` | DT3 | NEW |
| `dto/producto/ProductoRequestDTO.java` | DT3 | NEW |
| `dto/producto/ProductoResponseDTO.java` | DT3 | NEW |
| `dto/mapper/BodegaMapper.java` | DT3 | NEW |
| `dto/mapper/ProductoMapper.java` | DT3 | NEW |
| Todos los controladores y servicios | DT4 | MODIFY |
| Todos los archivos con `System.out.println` | DT5 | MODIFY |
| Todas las entidades con Lombok | DT6 | MODIFY |
| `listeners/AuditoriaListener.java` | DT7, DT8 | MODIFY |
| `services/AuditoriaErrorService.java` | DT8 | MODIFY |
| `services/DetalleMovimientoService.java` | DT8 | MODIFY |
| `services/SecurityContextService.java` | DT8 | NEW |
| Todos los controladores (paginación) | DT9 | MODIFY |
| `pom.xml` | DT10 | MODIFY (eliminar MySQL, agregar H2 + PostgreSQL) |
| `application.properties` | DT10 | MODIFY (reescribir con H2 como BD principal) |
| `application-prod.properties` | DT10 | NEW (perfil Supabase/PostgreSQL) |
| `data.sql` | DT10 | MODIFY (adaptar para H2) |
| `schema.sql` | DT10 | DELETE (ya no necesario con `ddl-auto`) |

---

## Orden de Ejecución Sugerido

```
DT6 (Warnings Lombok) → DT7 (Deprecated API)
    → DT1 (Bug Contraseña)
    → DT4 (Inyección Constructor)
    → DT5 (SLF4J Logger)
    → DT8 (Eliminar Duplicación)
    → DT3 (DTOs Completos) → DT2 (Movimientos Atómicos)
    → DT9 (Paginación)
    → DT10 (Perfil H2)
```

> Se empieza con las tareas más rápidas y de bajo riesgo (DT6, DT7) para generar momentum. DT1 es un fix funcional urgente. DT4 y DT5 son refactors mecánicos que preparan el terreno para DT8 y DT3, que son los cambios arquitectónicos más profundos.
