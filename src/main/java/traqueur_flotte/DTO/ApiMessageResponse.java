package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Message de confirmation d'une opération utilisateur. */
@Schema(name = "ApiMessage", description = "Message de confirmation de l'opération.")
public record ApiMessageResponse(
	@Schema(description = "Confirmation de l'opération", example = "Le mot de passe a été modifié.") String message) {
}