package traqueur_flotte.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import traqueur_flotte.model.Role;

/**
 * DTO (Data Transfer Object) : objet échangé entre le client et le serveur.
 * On n'expose jamais l'entité User directement dans le Controller.
 */
@Schema(name = "LegacyUserDTO", description = "DTO conservé pour la route historique de liste des utilisateurs.")
public class UserDTO {

    @Schema(description = "Identifiant utilisateur", example = "42")
    private Long id;
    @Schema(description = "Nom", example = "Martin")
    private String nom;
    @Schema(description = "Prénom", example = "Lea")
    private String prenom;
    @Schema(description = "Adresse email", example = "lea.martin@example.com")
    private String email;
    @Schema(hidden = true)
    private String motDePasse;
    @Schema(description = "Téléphone", example = "+33123456789")
    private String telephone;
    @Schema(description = "Rôle", example = "GESTIONNAIRE")
    private Role role;

    public UserDTO() {}

    /** Retourne l'identifiant utilisateur. */
    public Long getId() {
        return id;
    }

    /** Définit l'identifiant utilisateur. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Retourne le nom de famille. */
    public String getNom() {
        return nom;
    }

    /** Définit le nom de famille. */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /** Retourne le prénom. */
    public String getPrenom() {
        return prenom;
    }

    /** Définit le prénom. */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /** Retourne l'adresse email. */
    public String getEmail() {
        return email;
    }

    /** Définit l'adresse email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Retourne le mot de passe de la requête historique; ce champ ne doit jamais être sérialisé en réponse. */
    public String getMotDePasse() {
        return motDePasse;
    }

    /** Définit le mot de passe de la requête historique. */
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    /** Retourne le téléphone. */
    public String getTelephone() {
        return telephone;
    }

    /** Définit le téléphone. */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /** Retourne le rôle. */
    public Role getRole() {
        return role;
    }

    /** Définit le rôle. */
    public void setRole(Role role) {
        this.role = role;
    }
}
