package com.fitnessapp.fitapp_api.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    private final ObjectMapper objectMapper; // mapper global de Spring

    // Payload común para errores
    public Map<String, Object> payload(String error,
                                       String message,
                                       String path,
                                       Map<String, Object> details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", error);
        m.put("message", message);
        m.put("path", path);
        m.put("timestamp", OffsetDateTime.now().toString());
        if (details != null && !details.isEmpty()) m.put("details", details);
        return m;
    }

    // ResponseEntity para el @RestControllerAdvice
    public ResponseEntity<Object> entity(HttpStatus status,
                                         String error,
                                         String message,
                                         String path,
                                         Map<String, Object> details) {
        return ResponseEntity.status(status).body(payload(error, message, path, details));
    }

    // Escritura directa para EntryPoint/AccessDeniedHandler en Security
    public void write(HttpServletResponse res,
                      int status,
                      String error,
                      String message,
                      String path,
                      Map<String, Object> details) throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), payload(error, message, path, details));
    }
}