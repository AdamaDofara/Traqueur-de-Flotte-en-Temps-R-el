package traqueur_flotte.model;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Entité persistée représentant un utilisateur, son profil et ses données de session/réinitialisation. */
@Entity
@Table(name="Utilisateur")
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(nullable = false)
    private String name; 

    @Column(nullable = false)
    private String prenom; 

    @Column(nullable = false, unique = true)
    private String email; 

    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false)
    private boolean actif; 

    @Column(unique = true)
    private String passwordResetToken;

    private java.time.Instant passwordResetExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(unique = true)
    private String telephone;

    @OneToMany(mappedBy = "gestionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flotte> flottes = new ArrayList<>();

    @OneToOne(mappedBy = "conducteur")
    private Vehicule vehicule; 


    /** Constructeur requis par JPA. */
    public User() {}

    /** Retourne l'identifiant généré. */
    public Long getId() {
        return id;
    }

    /** Retourne le nom de famille. */
    public String getNom() {
        return name;
    }

    /** Définit le nom de famille. */
    public void setNom(String nom) {
        this.name = nom;
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

    /** Retourne le mot de passe hashé. */
    public String getMotDePasse() {
        return motDePasse;
    }

    /** Définit le mot de passe hashé; l'encodage est réalisé par le service. */
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    /** Indique si le compte est actif. */
    public boolean isActif() {
        return actif;
    }

    /** Définit l'état actif du compte. */
    public void setActif(boolean actif) {
        this.actif = actif;
    }

    /** Retourne le token courant de réinitialisation, s'il existe. */
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    /** Définit le token de réinitialisation. */
    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    /** Retourne l'échéance du token de réinitialisation. */
    public java.time.Instant getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    /** Définit l'échéance du token de réinitialisation. */
    public void setPasswordResetExpiresAt(java.time.Instant passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    /** Retourne le rôle attribué au compte. */
    public Role getRole() {
        return role;
    }

    /** Définit le rôle attribué au compte. */
    public void setRole(Role role) {
        this.role = role;
    }

    /** Retourne le téléphone, éventuellement absent. */
    public String getTelephone() {
        return telephone;
    }

    /** Définit le téléphone facultatif. */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /** Retourne les flottes gérées par cet utilisateur. */
    public List<Flotte> getFlottes() {
        return flottes;
    }

    /** Définit les flottes gérées par cet utilisateur. */
    public void setFlottes(List<Flotte> flottes) {
        this.flottes = flottes;
    }


}
