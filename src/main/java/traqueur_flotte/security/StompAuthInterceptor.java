package traqueur_flotte.security;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import traqueur_flotte.model.Role;
import traqueur_flotte.model.User;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;

@Component
@Profile("websocket-demo")
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final Pattern CANAL_FLOTTE =
            Pattern.compile("/topic/flottes/([1-9][0-9]*)/(?:positions|alertes)");

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtConverter;
    private final UserRepository userRepository;
    private final FlotteRepository flotteRepository;

    public StompAuthInterceptor(
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtConverter,
            UserRepository userRepository,
            FlotteRepository flotteRepository
    ) {
        this.jwtDecoder = jwtDecoder;
        this.jwtConverter = jwtConverter;
        this.userRepository = userRepository;
        this.flotteRepository = flotteRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headers == null || headers.getCommand() == null) {
            return message;
        }

        switch (headers.getCommand()) {
            case CONNECT, STOMP -> connecter(headers);
            case SEND -> {
                User utilisateur = utilisateurActif(authentication(headers));
                if (!"/app/positions".equals(headers.getDestination())
                        || utilisateur.getRole() != Role.CONDUCTEUR) {
                    throw new AccessDeniedException("Envoi STOMP interdit.");
                }
            }
            case SUBSCRIBE -> verifierAbonnement(headers);
            case UNSUBSCRIBE, DISCONNECT -> { }
            default -> throw new AccessDeniedException("Commande STOMP interdite.");
        }

        return message;
    }

    private void connecter(StompHeaderAccessor headers) {
        String authorization = headers.getFirstNativeHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("Jeton JWT requis.");
        }

        try {
            Jwt jwt = jwtDecoder.decode(authorization.substring("Bearer ".length()));
            var authentication = jwtConverter.convert(jwt);

            if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                throw new AccessDeniedException("Authentification JWT invalide.");
            }

            utilisateurActif(jwtAuthentication);
            headers.setUser(jwtAuthentication);
            headers.removeNativeHeader("Authorization");
        } catch (JwtException exception) {
            throw new AccessDeniedException("Jeton JWT invalide ou expiré.");
        }
    }

    private JwtAuthenticationToken authentication(StompHeaderAccessor headers) {
        if (headers.getUser() instanceof JwtAuthenticationToken authentication) {
            return authentication;
        }

        throw new AccessDeniedException("Connexion STOMP non authentifiée.");
    }

    private User utilisateurActif(JwtAuthenticationToken authentication) {
        Instant expiration = authentication.getToken().getExpiresAt();
        if (expiration == null || !expiration.isAfter(Instant.now())) {
            throw new AccessDeniedException("Jeton JWT expiré.");
        }

        Long utilisateurId = lireId(authentication.getToken().getSubject());

        return userRepository.findById(utilisateurId)
                .filter(User::isActif)
                .orElseThrow(() -> new AccessDeniedException("Compte inaccessible."));
    }

    private void verifierAbonnement(StompHeaderAccessor headers) {
        User utilisateur = utilisateurActif(authentication(headers));
        String destination = headers.getDestination();

        if ("/user/queue/positions-erreurs".equals(destination)) {
            return;
        }

        Matcher canal = CANAL_FLOTTE.matcher(destination == null ? "" : destination);
        if (!canal.matches()) {
            throw new AccessDeniedException("Abonnement STOMP interdit.");
        }

        Long flotteId = lireId(canal.group(1));

        if (utilisateur.getRole() == Role.ADMINISTRATEUR
                && flotteRepository.existsById(flotteId)) {
            return;
        }

        if (utilisateur.getRole() == Role.GESTIONNAIRE
                && flotteRepository.existsByIdAndGestionnaire_Id(flotteId, utilisateur.getId())) {
            return;
        }

        throw new AccessDeniedException("Flotte inaccessible.");
    }

    private Long lireId(String valeur) {
        try {
            return Long.valueOf(valeur);
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException("Identifiant invalide.");
        }
    }
}