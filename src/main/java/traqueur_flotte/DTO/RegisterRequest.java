package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import traqueur_flotte.model.Role;

/** Données requises pour créer un compte utilisateur. */
@Schema(name = "RegisterRequest", description = "Informations d'inscription. Le téléphone est facultatif.")
public record RegisterRequest(
        @Schema(description = "Nom de famille", example = "Martin") String nom,
        @Schema(description = "Prénom", example = "Lea") String prenom,
        @Schema(description = "Adresse email unique", example = "lea.martin@example.com") String email,
        @Schema(description = "Mot de passe en clair à transmettre uniquement en HTTPS, minimum 8 caractères", example = "Secret123") String motDePasse,
        @Schema(description = "Rôle du compte", example = "GESTIONNAIRE") Role role,
        @Schema(description = "Numéro de téléphone facultatif", example = "+33123456789", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String telephone) {
}