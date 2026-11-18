package traqueur_flotte.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import traqueur_flotte.DTO.UserResponseDTO;

/** Émet des access tokens JWT HS256 pour les utilisateurs authentifiés. */
@Service
public class JwtService {

    private static final String ISSUER = "traqueur-flotte";

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${JWT_ACCESS_TOKEN_MINUTES:15}") long expirationMinutes) {
        if (expirationMinutes < 1) {
            throw new IllegalArgumentException("JWT_ACCESS_TOKEN_MINUTES doit être supérieur à zéro.");
        }
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    /** Génère un token limité en durée, identifié par l'id utilisateur et portant son rôle. */
    public IssuedToken generateToken(UserResponseDTO user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of("traqueur-flotte-api"))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .subject(user.id().toString())
                .claim("roles", List.of(user.role().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, expirationMinutes * 60);
    }

    /** Access token émis et durée de validité en secondes. */
    public record IssuedToken(String value, long expiresInSeconds) {
    }
}