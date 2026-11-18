package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/** Champs modifiables du profil, sans changement de rôle ni de mot de passe. */
@Schema(name = "UpdateProfilRequest", description = "Nouvelles informations du profil utilisateur.")
public record UpdateProfilRequest(
	@Schema(description = "Nom de famille", example = "Martin", requiredMode = Schema.RequiredMode.REQUIRED) String nom,
	@Schema(description = "Prénom", example = "Lea", requiredMode = Schema.RequiredMode.REQUIRED) String prenom,
	@Schema(description = "Adresse email unique", example = "lea.martin@example.com", requiredMode = Schema.RequiredMode.REQUIRED) String email,
	@Schema(description = "Téléphone; si absent, la valeur actuelle est conservée", example = "+33123456789", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String telephone) {
}