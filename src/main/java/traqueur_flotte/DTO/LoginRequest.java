package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Identifiants nécessaires à l'ouverture d'une session. */
@Schema(name = "LoginRequest", description = "Identifiants utilisateur; la réponse établit une session HTTP.")
public record LoginRequest(
	@Schema(description = "Adresse email du compte", example = "lea.martin@example.com", requiredMode = Schema.RequiredMode.REQUIRED) String email,
	@Schema(description = "Mot de passe en clair, transmis uniquement sur HTTPS", example = "Secret123", requiredMode = Schema.RequiredMode.REQUIRED) String motDePasse) {
}