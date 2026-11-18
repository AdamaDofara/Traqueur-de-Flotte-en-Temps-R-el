package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Réponse de connexion contenant l'access token et le profil public. */
@Schema(name = "AuthResponse", description = "Access token Bearer JWT et profil utilisateur.")
public record AuthResponse(
	@Schema(description = "JWT signé à envoyer en Bearer dans l'en-tête Authorization") String accessToken,
	@Schema(description = "Type du token", example = "Bearer") String tokenType,
	@Schema(description = "Durée de validité en secondes", example = "900") long expiresIn,
	@Schema(description = "Profil public de l'utilisateur connecté") UserResponseDTO utilisateur,
	@Schema(description = "Confirmation de la connexion", example = "Connexion réussie.") String message) {
}