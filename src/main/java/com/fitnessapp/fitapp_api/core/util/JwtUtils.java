package com.fitnessapp.fitapp_api.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {

    //Con estas configuraciones aseguramos la autenticidad del token a crear
    @Value("${security.jwt.private.key}")
    private String privateKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;
    @Value("${security.jwt.ttl-seconds}")
    private long ttlSeconds;

    //Para encriptar, vamos a necesitar esta clave secreta y este algoritmo
    public String createToken(Authentication authentication) {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        //esto está dentro del security context holder
        String email = authentication.getName();

        //también obtenemos los permisos/autorizaciones. Como Strings separados por coma o como Arrays.
        String[] authArray = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        Instant now = Instant.now();

        Date iat = Date.from(now); //IssuedAt
        Date exp = Date.from(now.plusSeconds(ttlSeconds)); //30 min. ExpiresAt

        //a partir de esto generamos el token
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator) //Generador del Token (Issuer)
                .withSubject(email) // Propietario del Token (el email unico del usuario)
                .withArrayClaim("authorities", authArray)// Claims, datos contraidos en el JWT
                .withIssuedAt(iat) // Fecha de generación del token
                .withExpiresAt(exp) // Fecha de expiración, tiempo en milisegundos
                .withJWTId(UUID.randomUUID().toString()) // Id del token
                .withNotBefore(iat) // Desde cuando es válido (desde ahora en este caso)
                .sign(algorithm); // Nuestra firma que creamos antes con la clave secreta

        return jwtToken;
    }

    // Validar y decodificar token
    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privateKey);
            // Construimos un verifier con el algoritmo y el Issuer igual que los que generan el token
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();
            // Si esta bien no da excepción y devuelve el JWT decodificado
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (IllegalArgumentException e) {
            // clave nula o mal formada -> Algorithm.HMAC256 puede tirar esto
            throw new JWTVerificationException("Invalid token configuration", e);
        }
    }

    // Extraer el email del token decodificado
    public String extractEmail(DecodedJWT decodedJWT) {
        //el subject es el usuario según establecimos al crear el token, entonces es el email.
        return decodedJWT.getSubject();
    }

    // Obtener todos los claims
    public Map<String, Claim> returnAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

    // Obtener un claim en específico
    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }
}