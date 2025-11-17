# Sistema de Gestión de Bodegas

👥 Autores

David Alejandro Ardila Cardozo
Nicolás Felipe Arrubla Chaux

## 📋 Descripción del Proyecto

Sistema integral de gestión de inventario y bodegas desarrollado con **Spring Boot 3.5.7** y **MySQL**. Implementa un sistema completo de autenticación JWT, auditoría de operaciones, control de acceso basado en roles (RBAC) y gestión avanzada de movimientos de inventario.

### ✨ Características Principales

- **🔐 Autenticación y Autorización JWT**
  - Sistema de tokens seguros con expiración configurable
  - Control de acceso basado en roles (ADMIN, ENCARGADO, OPERADOR)
  - Encriptación de contraseñas con BCrypt

- **📊 Gestión de Inventario**
  - Control de bodegas con capacidad y ubicación
  - Gestión de productos con categorías y precios
  - Movimientos de inventario (ENTRADA, SALIDA, TRANSFERENCIA)
  - Validación automática de stock y capacidad

- **📝 Auditoría Completa**
  - Registro automático de todas las operaciones (INSERT, UPDATE, DELETE)
  - Tracking de intentos fallidos con detalles del error
  - Historial de cambios con valores anterior y nuevo
  - Identificación de usuario responsable

- **🎨 Frontend Moderno**
  - Interfaz responsive con dashboards personalizados por rol
  - Consumo de API REST con JavaScript vanilla
  - Diseño moderno con gradientes y animaciones CSS

- **📚 Documentación API**
  - Swagger UI integrado (`/swagger-ui.html`)
  - Especificación OpenAPI 3.0
  - Endpoints documentados y probables

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.5.7**
  - Spring Data JPA
  - Spring Security
  - Spring Web
  - Spring Validation
- **MySQL 8.0**
- **JWT (JSON Web Tokens)** - io.jsonwebtoken:jjwt
- **Lombok** - Reducción de código boilerplate
- **Swagger/OpenAPI** - Documentación interactiva

### Frontend
- **HTML5** / **CSS3** / **JavaScript ES6+**
- **Fetch API** - Consumo de servicios REST
- **LocalStorage** - Gestión de tokens y sesión

---

## 📦 Instalación y Configuración

### Prerrequisitos
```bash
- Java 17 o superior
- MySQL 8.0 o superior
- Maven 3.9+
- Git
```

### 1️⃣ Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/gestion-bodegas.git
cd gestion-bodegas
```

### 2️⃣ Configurar Base de Datos

Crear la base de datos en MySQL:
```sql
CREATE DATABASE gestion_bodegas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configurar credenciales en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_bodegas
spring.datasource.username=root
spring.datasource.password=tu_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

jwt.secret=TuClaveSecretaSuperSegura1234567890
jwt.expiration-minutes=60
```

### 3️⃣ Compilar y Ejecutar

**Opción A: Usando Maven Wrapper (recomendado)**
```bash
# Windows
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean package
./mvnw spring-boot:run
```

**Opción B: Maven instalado localmente**
```bash
mvn clean package
mvn spring-boot:run
```

**Opción C: Despliegue en Tomcat externo**
```bash
# Compilar WAR
.\mvnw.cmd clean package -DskipTests

# Desplegar automáticamente (Windows)
.\deploy.bat

# El archivo WAR estará en: target/gestionbodegas-0.0.1-SNAPSHOT.war
```

### 4️⃣ Verificar Instalación

- **Aplicación**: http://localhost:8080
- **Frontend**: http://localhost:8080/html/login.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 🚀 Uso del Sistema

### Credenciales de Prueba (después de ejecutar data.sql)
```
👨‍💼 ADMIN
Usuario: cgomez
Contraseña: admin123

👔 ENCARGADO
Usuario: lperez
Contraseña: encargado123

👷 OPERADOR
Usuario: matorres
Contraseña: operador123
```

### Flujo de Autenticación

1. **Registro** (opcional): `POST /api/auth/register`
2. **Login**: `POST /api/auth/login` → Retorna JWT token
3. **Uso de endpoints protegidos**: Incluir header `Authorization: Bearer {token}`

---

## 📡 Ejemplos de Endpoints

### 🔐 Autenticación

#### Login
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "cgomez",
  "password": "admin123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJBRE1JTiIsInN1YiI6ImNnb21leiIsImlhdCI6MTczMjUwMDAwMCwiZXhwIjoxNzMyNTAzNjAwfQ..."
}
```

#### Registro
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "password": "password123",
  "nombreCompleto": "Usuario Nuevo",
  "rol": "OPERADOR"
}
```

---

### 🏢 Bodegas

#### Obtener todas las bodegas
```http
GET http://localhost:8080/api/bodegas
Authorization: Bearer {tu_token}
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "nombre": "Bodega Central",
    "ubicacion": "Zona Industrial Norte, Medellín",
    "capacidad": 5000,
    "encargado": {
      "id": 2,
      "username": "lperez",
      "nombreCompleto": "Laura Pérez Martínez"
    }
  }
]
```

#### Crear bodega
```http
POST http://localhost:8080/api/bodegas
Authorization: Bearer {tu_token}
Content-Type: application/json

{
  "nombre": "Bodega Zona Franca",
  "ubicacion": "Km 15 Vía al Aeropuerto",
  "capacidad": 3500,
  "encargado": {
    "id": 2
  }
}
```

#### Actualizar bodega
```http
PUT http://localhost:8080/api/bodegas/1
Authorization: Bearer {tu_token}
Content-Type: application/json

{
  "nombre": "Bodega Central Renovada",
  "ubicacion": "Zona Industrial Norte, Medellín",
  "capacidad": 6000,
  "encargado": {
    "id": 2
  }
}
```

---

### 📦 Productos

#### Obtener todos los productos
```http
GET http://localhost:8080/api/productos
Authorization: Bearer {tu_token}
```

#### Crear producto
```http
POST http://localhost:8080/api/productos
Authorization: Bearer {tu_token}
Content-Type: application/json

{
  "nombre": "Laptop HP Pavilion",
  "categoria": "Computadoras",
  "stock": 25,
  "precio": 2800000.00,
  "bodega": {
    "id": 1
  }
}
```

#### Productos con stock bajo
```http
GET http://localhost:8080/api/productos/stock-bajo/10
Authorization: Bearer {tu_token}
```

---

### ↔️ Movimientos de Inventario

#### Crear movimiento de ENTRADA
```http
POST http://localhost:8080/api/movimientos
Authorization: Bearer {tu_token}
Content-Type: application/json

{
  "tipo": "ENTRADA",
  "usuario": {
    "id": 1
  },
  "bodegaDestino": {
    "id": 1
  }
}
```

**Luego crear el detalle:**
```http
POST http://localhost:8080/api/detalle-movimientos
Authorization: Bearer {tu_token}
Content-Type: application/json

{
  "movimiento": {
    "id": 1
  },
  "producto": {
    "id": 1
  },
  "cantidad": 50
}
```

---

### 👥 Usuarios

#### Obtener todos los usuarios
```http
GET http://localhost:8080/api/usuarios
Authorization: Bearer {tu_token}
```

#### Buscar usuario por username
```http
GET http://localhost:8080/api/usuarios/username/cgomez
Authorization: Bearer {tu_token}
```

---

### 📋 Auditoría

#### Obtener todas las auditorías
```http
GET http://localhost:8080/api/auditorias
Authorization: Bearer {tu_token}
```

#### Auditorías por tipo de operación
```http
GET http://localhost:8080/api/auditorias/tipo/INSERT
Authorization: Bearer {tu_token}
```

---

### 📊 Reportes

#### Resumen general del sistema
```http
GET http://localhost:8080/api/reportes/resumen-general
Authorization: Bearer {tu_token}
```

**Respuesta:**
```json
{
  "stockPorBodega": [
    ["Bodega Central", 215],
    ["Bodega Norte", 143]
  ],
  "productosMasMovidos": [
    ["Monitor LG 27\"", 120],
    ["Teclado Mecánico", 95]
  ],
  "productosStockBajo": [
    [4, "Silla Gamer Cougar", 15]
  ],
  "totalBodegas": 5,
  "totalProductos": 8,
  "totalMovimientos": 6
}
```

---

## 📸 Capturas de Pantalla

### Swagger UI
![Swagger UI](docs/images/swagger-ui.png)
*Documentación interactiva de la API con Swagger*

### Interfaz de Login
![Login](docs/images/login.png)
*Pantalla de autenticación con diseño moderno*

### Dashboard Administrativo
![Dashboard Admin](docs/images/dashboard-admin.png)
*Panel de control para administradores con métricas en tiempo real*

### Gestión de Productos
![Productos](docs/images/productos.png)
*Vista de gestión completa de productos con operaciones CRUD*

### Registro de Movimientos
![Movimientos](docs/images/movimientos.png)
*Historial de movimientos de inventario con filtros*

---

## 🎨 Frontend

El frontend está ubicado en `src/main/resources/` y consta de:

### Estructura de Archivos
```
resources/
├── html/
│   ├── login.html              # Página de inicio de sesión
│   ├── register.html           # Registro de nuevos usuarios
│   ├── dashboard_admin.html    # Dashboard para administradores
│   ├── dashboard_encargado.html # Dashboard para encargados
│   └── dashboard_operador.html  # Dashboard para operadores
├── css/
│   ├── login.css               # Estilos para login/register
│   └── dashboard.css           # Estilos para dashboards
└── js/
    ├── login.js                # Lógica de autenticación
    ├── register.js             # Lógica de registro
    ├── dashboard.js            # Dashboard ADMIN
    ├── dashboard_encargado.js  # Dashboard ENCARGADO
    └── dashboard_operador.js   # Dashboard OPERADOR
```

### Características del Frontend

- **Diseño Responsive**: Compatible con dispositivos móviles y escritorio
- **Dashboards por Rol**: Cada rol tiene su propia vista personalizada
- **Gestión de Tokens**: Almacenamiento seguro en LocalStorage
- **CRUD Completo**: Crear, leer, actualizar y eliminar registros
- **Validación de Formularios**: Validación en cliente y servidor
- **Manejo de Errores**: Mensajes claros y amigables

### Acceso al Frontend
```
http://localhost:8080/html/login.html
```

---

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest=UsuarioServiceTest
```

### Colección Postman

Importar la colección desde: `docs/postman/Gestion-Bodegas.postman_collection.json`

---

## 🔧 Configuración Avanzada

### Cambiar Puerto del Servidor
```properties
# application.properties
server.port=9090
```

### Configurar Tiempo de Expiración JWT
```properties
jwt.expiration-minutes=120  # 2 horas
```

### Habilitar SQL Logging
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

---

## 📝 Notas Importantes

### Validaciones de Negocio

1. **Stock Insuficiente**: No se permite SALIDA si no hay stock disponible
2. **Capacidad Excedida**: ENTRADA rechazada si excede capacidad de bodega
3. **Producto Inexistente**: TRANSFERENCIA requiere que el producto exista en bodega origen
4. **Validación de Roles**: Cada endpoint valida permisos según rol del usuario

### Auditoría Automática

Todas las operaciones CUD (Create, Update, Delete) son auditadas automáticamente:
- **Entidad afectada**: Nombre de la clase
- **Usuario responsable**: Del contexto de seguridad
- **Valores**: JSON con estado anterior y nuevo
- **Timestamp**: Fecha y hora exacta

### Intentos Fallidos

Los intentos de movimientos que fallan por validación se registran en `intentos_fallidos`:
- Tipo de movimiento intentado
- Razón del fallo
- Datos del intento (producto, cantidad, bodegas)
- Usuario que intentó la operación

---

## 📚 Documentación Técnica Completa

Para información detallada sobre:
- Arquitectura del sistema
- Diagramas de clases
- Funcionamiento de JWT
- Diagramas de secuencia
- Modelo de datos completo

Consultar el documento: **[Documentación Técnica (PDF)](docs/DOCUMENTACION_TECNICA.pdf)**

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la Licencia Apache 2.0. Ver archivo `LICENSE` para más detalles.

---

## 👥 Autores

- **Equipo de Desarrollo C3**
- Email: soporte@c3.com

---

## 🐛 Reporte de Bugs

Para reportar bugs o solicitar features, crear un issue en GitHub.

---

## 📚 Recursos Adicionales

- [Documentación Spring Boot](https://spring.io/projects/spring-boot)
- [JWT Introduction](https://jwt.io/introduction)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

## ⭐ Roadmap

- [ ] Implementar paginación en listados
- [ ] Agregar filtros avanzados de búsqueda
- [ ] Exportación de reportes a Excel/PDF
- [ ] Notificaciones por email
- [ ] Dashboard con gráficos estadísticos
- [ ] API de integración con sistemas externos
- [ ] Aplicación móvil

---

**¿Preguntas o sugerencias?** No dudes en contactarnos o abrir un issue en GitHub.