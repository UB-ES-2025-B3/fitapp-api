# FitApp-Api

## Configuración de variables de entorno
Este proyecto **no** versiona secretos. Usa **`.env.example`** como plantilla
y crea tu `.env` local.

**No** subas tu .env al repositorio y, en caso de que no lo esté, añade `.env` a
tu **`.gitignore`**.

Una vez añadido, IntelliJ proporciona de plugins para leer archivos `.env`, como **EnvFile**.

## Configuración de base de datos
La base de datos ahora se carga automaticamente con Docker Compose. La
configuración se encuentra en el `docker-compose.yml`. 
Configurad vuestro `.env` con el user y password que querais usar para acceder a la base de datos.
**Recordad que se ejecuta en el puerto 3307.**

Antes de ejecutar la aplicacion de Spring, aseguraos de tener **Docker y Docker Compose instalados** y
ejecutad el comando:

```
docker-compose up -d
```

Se puede acceder a la interfaz de phpMyAdmin en `http://localhost:8081` con las credenciales definidas
en el archivo `docker-compose.yml`.

## Estructura del Proyecto

La estructura es **feature-first**: cada “feature” agrupa su controller, service, repos,
entidades y DTOs, y lo transversal vive en `core/` y `config/`.

```
src/main/java/com/fitnessapp/fitapp_api
├─ FitAppApiApplication.java
├─ config/                      # CORS y alguna cosa más.
│   ├─ WebConfig.java
│   └─ OpenApiConfig.java
├─ core/                        # Infraestructura compartida
│   ├─ exception/               # Excepciones de dominio/API
│   ├─ handler/                 # @RestControllerAdvice (Para manejar los errores)
│   ├─ security/                # SecurityConfig, JwtUtils, filtros(JWT) y helpers(para comprobaciones).
│   ├─ validation/              # Validadores personalizados, ValidationMessages.properties
│   └─ util/                    # Helpers genéricos
│
├─ auth/                        # Feature: registro/login (autenticación)
│   ├─ controller/              # Controlador: endpoints
│   ├─ service/                 # Logica de negocio
│   ├─ model/                   # Entidades JPA (UsuarioAuth, Role si acabamos usando)
│   ├─ repository/              # Conexión con la BD.
│   └─ dto/                     # Requests/Responses del feature. El Controller recibe y envia DTOs
│
└─ profile/                     # Feature: perfil del usuario
│   ├─ controller/
│   ├─ service/
│   ├─ model/                   # Profile (Info del perfil de usuario)
│   ├─ repository/
│   └─ dto/
│
└─...

```
**Resources:**
```
src/main/resources/
├─ application.properties       # Config principal (usa variables de entorno)
└─ db/migration/                # Migraciones de Flyway. Cambios en Entidades
   └─ V1__init.sql
```
### Convenciones

- **Paquetes por feature**: `auth`, `profile`,...
- **Capas dentro del feature**: `controller/`, `service/`, `model/`, `repository/`, `dto/`.
- **DTOs ≠ Entidades**: No exponer entidades JPA en los controladores.
- **Transacciones**: En los *services*; los *controllers* solo orquestan.
- **Validación**: Bean Validation en DTOs; errores coherentes vía `GlobalExceptionHandler`.
- **Seguridad**: Protección en `core/security` y/o anotaciones `@PreAuthorize`.
- **/config**: WebConfig: CORS global consumido por Security; OpenApiConfig: metadatos opcionales de Swagger.
- **Nomenclatura (interfaces/impls)**:
    - Interfaz: nombre sin prefijos, describe el contrato → `UserService`.
    - Implementación: `UserServiceImpl`.
- **Inyección de dependencias**: 
  - Inyecta SIEMPRE la interfaz, nunca la implementación concreta, en controllers y services.
    → Favorece el desacoplamiento, testeo y cambios de estrategia sin tocar consumidores. 
  - Usa inyección por constructor (Lombok @RequiredArgsConstructor) y campos *final*. Evitar uso de @Autowired.

## Seguridad & CORS

- API stateless con JWT (csrf off; sin httpBasic/formLogin).
- CORS global en config/WebConfig leyendo app.cors.origins.
- JWT: filtro JwtTokenValidator (antes de UsernamePasswordAuthenticationFilter).
- Errores JSON consistentes (ErrorResponseFactory + GlobalExceptionHandler).
- En caso de añadir una Excepción personalizada, asegurarse de que se trate en el `GlobalExceptionHandler`.
### Rutas públicas
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html
- OPTIONS /**

El resto requiere `Authorization: Bearer <jwt>`.

### Formato de error
Manejado por ErrorResponseFactory.

Ejemplo de error:
{ "error": "...", "message": "...", "path": "/...", "timestamp": "ISO-8601", "details"[] }

- "details": es opcional, sale en errores de validación de datos en las requests.