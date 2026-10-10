package traqueur_flotte.DTO;

import traqueur_flotte.model.Role;

/**
 * DTO (Data Transfer Object) : objet échangé entre le client et le serveur.
 * On n'expose jamais l'entité User directement dans le Controller.
 */
public class UserDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private Role role;

    public UserDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
