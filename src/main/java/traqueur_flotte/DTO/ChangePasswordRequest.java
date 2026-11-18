package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Ancien et nouveau secrets pour le changement de mot de passe authentifié. */
@Schema(name = "ChangePasswordRequest", description = "Le nouveau mot de passe doit avoir au moins 8 caractères.")
public record ChangePasswordRequest(
	@Schema(description = "Mot de passe courant", example = "Secret123", requiredMode = Schema.RequiredMode.REQUIRED) String ancienMotDePasse,
	@Schema(description = "Nouveau mot de passe d'au moins 8 caractères", example = "AutreSecret123", requiredMode = Schema.RequiredMode.REQUIRED) String nouveauMotDePasse) {
}