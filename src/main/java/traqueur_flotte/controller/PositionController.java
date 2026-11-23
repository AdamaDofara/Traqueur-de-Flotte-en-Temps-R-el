package traqueur_flotte.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.services.PositionConsultationService;

@RestController
@RequestMapping("/api/flottes")
public class PositionController {

    private final PositionConsultationService consultationService;

    public PositionController(PositionConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/{flotteId}/positions")
    @Operation(summary = "Consulter les dernières positions connues d'une flotte")
    @SecurityRequirement(name = "bearerAuth")
    public List<PositionGpsMessage> listerPositions(
            @PathVariable("flotteId") Long flotteId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long utilisateurId;
        try {
            utilisateurId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Identifiant JWT invalide."
            );
        }

        return consultationService.listerParFlotte(flotteId, utilisateurId);
    }
}