package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Format d'erreur renvoyé par les erreurs métier utilisateur. */
@Schema(name = "ApiError", description = "Message décrivant l'erreur retournée par l'API.")
public record ApiErrorResponse(
	@Schema(description = "Explication lisible de l'erreur", example = "Utilisateur introuvable.") String erreur) {
}