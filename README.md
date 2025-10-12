# FitApp-Api

## Configuración de variables de entorno
Este proyecto **no** versiona secretos. Usa **`.env.example`** como plantilla
y crea tu `.env` local.

**No** subas tu .env al repositorio y, en caso de que no lo esté, añade `.env` a
tu **`.gitignore`**.

Una vez añadido, IntelliJ proporciona de plugins para leer archivos `.env`, como **EnvFile**.

## Configuración de base de datos
Creaos una base de datos MySQL con lo que querais y el nombre que querais (p.e. fitapp)
y poned la URL en vuestro `.env` junto al user y la password para acceder a ella.

Actualizacion: la base de datos ahora se carga automaticamente con Docker Compose. La
configuracion se encuentra en el .yml. Antes de ejecutar la 
aplicacion de spring, aseguraos de tener Docker y Docker Compose instalados y ejecutad el comando:

```
docker-compose up -d
```

Se puede acceder a la interfaz de phpMyAdmin en `http://localhost:8081` con las credenciales definidas en el archivo `docker-compose.yml`.

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