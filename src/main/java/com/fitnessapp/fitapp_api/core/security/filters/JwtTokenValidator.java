package com.fitnessapp.fitapp_api.core.security.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fitnessapp.fitapp_api.core.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


// Filtro que se ejecutara una vez por cada request
@RequiredArgsConstructor
public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AuthenticationEntryPoint authEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Si ya hay autenticación previa en el contexto, seguimos sin revalidar
        var existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing == null || existing instanceof AnonymousAuthenticationToken) {

            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                String trimmed = header.trim();
                // soporta "Bearer " case-insensitive
                if (trimmed.length() >= 7 && trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
                    String jwtToken = trimmed.substring(7).trim(); // "Bearer " + token
                    if (jwtToken.isEmpty()) {
                        SecurityContextHolder.clearContext();
                        authEntryPoint.commence(request, response,
                                new InsufficientAuthenticationException("Missing bearer token"));
                        return;
                    }
                    try {
                        DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

                        String email = jwtUtils.extractEmail(decodedJWT);
                        List<String> authoritiesClaim = jwtUtils
                                .getSpecificClaim(decodedJWT, "authorities")
                                .asList(String.class);

                        Collection<? extends GrantedAuthority> authoritiesList =
                                (authoritiesClaim == null ? List.<String>of() : authoritiesClaim).stream()
                                        // .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // si algún día guardas sin prefijo
                                        .map(SimpleGrantedAuthority::new)
                                        .toList();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authoritiesList);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);

                    } catch (JWTVerificationException ex) {
                        SecurityContextHolder.clearContext();
                        authEntryPoint.commence(
                                request, response,
                                new InsufficientAuthenticationException("Invalid token", ex)
                        );
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Si es preflight (OPTIONS con cabeceras CORS), NO ejecutes el filtro JWT
        return CorsUtils.isPreFlightRequest(request);
    }
}