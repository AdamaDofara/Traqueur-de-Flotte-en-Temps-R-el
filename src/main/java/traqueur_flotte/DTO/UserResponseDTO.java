package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import traqueur_flotte.model.Role;

/** Profil public retourné par les opérations utilisateur; aucun secret n'est inclus. */
@Schema(name = "UserResponse", description = "Profil utilisateur sans mot de passe ni token de réinitialisation.")
public record UserResponseDTO(
        @Schema(description = "Identifiant utilisateur", example = "42") Long id,
        @Schema(description = "Nom", example = "Martin") String nom,
        @Schema(description = "Prénom", example = "Lea") String prenom,
        @Schema(description = "Adresse email", example = "lea.martin@example.com") String email,
        @Schema(description = "Téléphone, si renseigné", example = "+33123456789") String telephone,
        @Schema(description = "Rôle du compte", example = "GESTIONNAIRE") Role role,
        @Schema(description = "Indique si le compte est actif", example = "true") boolean actif) {
}