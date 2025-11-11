package com.fitnessapp.fitapp_api.core.handler;

import com.fitnessapp.fitapp_api.core.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ErrorResponseFactory errorFactory;

    /**
     * 401 — Login fallido (credenciales incorrectas o usuario inexistente)
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Object> handleAuthFailures(RuntimeException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.UNAUTHORIZED, "unauthorized",
                "Invalid username or password", req.getRequestURI(), Map.of());
    }

    /**
     * 401 — Usuario deshabilitado (no puede autenticarse)
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabled(DisabledException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.UNAUTHORIZED, "disabled",
                "User account is disabled", req.getRequestURI(), Map.of());
    }

    /**
     * 423 — Cuenta bloqueada
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> handleLocked(LockedException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.LOCKED, "locked",
                "Account locked", req.getRequestURI(), Map.of());
    }

    /**
     * 401 — Credenciales expiradas
     */
    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<Object> handleCredsExpired(CredentialsExpiredException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.UNAUTHORIZED, "credentials_expired",
                "Credentials expired", req.getRequestURI(), Map.of());
    }

    /**
     * 403 — Acceso denegado (URL)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                                     HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.FORBIDDEN, "forbidden",
                "Access denied", req.getRequestURI(), Map.of());
    }

    /**
     * 403 — Autorización denegada (métodos con @PreAuthorize, etc.)
     */
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthzDenied(
            org.springframework.security.authorization.AuthorizationDeniedException ex,
            HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.FORBIDDEN, "forbidden",
                "Access denied", req.getRequestURI(), Map.of());
    }

    /**
     * 400 — JSON mal formado en @RequestBody
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String path = (request instanceof ServletWebRequest sw) ? sw.getRequest().getRequestURI() : "";
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "Malformed JSON";
        log.debug("Malformed JSON at {}: {}", path, msg);
        return errorFactory.entity(HttpStatus.BAD_REQUEST, "bad_request",
                "Malformed JSON request", path, Map.of());
    }

    /**
     * 400 — Bean Validation fallida en @RequestBody (con @Valid/@Validated)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
        String path = (request instanceof ServletWebRequest sw) ? sw.getRequest().getRequestURI() : "";
        log.debug("Validation failed at {}: {}", path, fields);
        return errorFactory.entity(HttpStatus.BAD_REQUEST, "bad_request",
                "Validation failed", path, Map.of("fields", fields));
    }

    /**
     * 400 — Bean Validation fallida en @PathVariable/@RequestParam (con @Validated en el controller)
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex,
                                                            HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                fields.put(v.getPropertyPath().toString(), v.getMessage()));
        return errorFactory.entity(HttpStatus.BAD_REQUEST, "bad_request",
                "Validation failed", req.getRequestURI(), Map.of("fields", fields));
    }

    /**
     * 409 — Violación de integridad (unique, FK, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String hint = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.debug("DataIntegrityViolation at {}: {}", req.getRequestURI(), hint);
        return errorFactory.entity(HttpStatus.CONFLICT, "conflict",
                "Data integrity violation", req.getRequestURI(), Map.of());
    }

    /**
     * 409 - Usuario (email) ya existe (registro)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.CONFLICT, "user_already_exists",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }

    /**
     * 500 — Fallback general (errores no contemplados)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAll(RuntimeException ex, HttpServletRequest req) {
        log.error("Unhandled error at {}:", req.getRequestURI(), ex);
        return errorFactory.entity(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                "Unexpected error", req.getRequestURI(), Map.of());
    }

    /**
     * 404 - Perfil de usuario no encontrado
     */
    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<Object> handleUserProfileNotFound(UserProfileNotFoundException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.NOT_FOUND, "user_profile_not_found",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }

    /**
     * 404 - Usuario no encontrado
     */
    @ExceptionHandler(UserAuthNotFoundException.class)
    public ResponseEntity<Object> handleUserAuthNotFound(UserAuthNotFoundException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.NOT_FOUND, "user_not_found",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }

    /**
     * 409 - Perfil ya existe para el usuario
     */
    @ExceptionHandler(UserProfileAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserProfileAlreadyExists(UserProfileAlreadyExistsException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.CONFLICT, "user_profile_already_exists",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }

    /**
     * Manejo genérico de ResponseStatusException lanzadas en servicios o controladores
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = switch (status) {
            case NOT_FOUND -> "not_found";
            case BAD_REQUEST -> "bad_request";
            case CONFLICT -> "conflict";
            default -> "error";
        };
        return errorFactory.entity(status, code,
                ex.getReason() != null ? ex.getReason() : "Error", req.getRequestURI(), Map.of());
    }

    /**
     * 404 - Ruta no encontrada
     */
    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Object> handleRouteNotFound(RouteNotFoundException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.NOT_FOUND, "route_not_found",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }

    /**
     * 404 - Ejecucion de ruta no encontrada
     */
    @ExceptionHandler(RouteExecutionNotFoundException.class)
    public ResponseEntity<Object> handleRouteExecutionNotFound(RouteExecutionNotFoundException ex, HttpServletRequest req) {
        return errorFactory.entity(HttpStatus.NOT_FOUND, "route_execution_not_found",
                ex.getMessage(), req.getRequestURI(), Map.of());
    }
}