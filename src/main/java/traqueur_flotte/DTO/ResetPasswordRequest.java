package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Token temporaire et nouveau mot de passe pour finaliser la réinitialisation. */
@Schema(name = "ResetPasswordRequest", description = "Le token reçu dans les logs expire après 30 minutes.")
public record ResetPasswordRequest(
	@Schema(description = "Token UUID obtenu dans les logs de l'application", example = "9d713b50-7f6d-4ed1-8742-5c53ed550b11", requiredMode = Schema.RequiredMode.REQUIRED) String token,
	@Schema(description = "Nouveau mot de passe d'au moins 8 caractères", example = "NouveauSecret123", requiredMode = Schema.RequiredMode.REQUIRED) String nouveauMotDePasse) {
}