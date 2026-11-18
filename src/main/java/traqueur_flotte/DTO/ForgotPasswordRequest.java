package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Adresse du compte demandant une réinitialisation de mot de passe. */
@Schema(name = "ForgotPasswordRequest", description = "L'API journalise un token temporaire pour cet email.")
public record ForgotPasswordRequest(
	@Schema(description = "Email du compte à réinitialiser", example = "lea.martin@example.com", requiredMode = Schema.RequiredMode.REQUIRED) String email) {
}