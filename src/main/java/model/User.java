package model;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="Utilisateur")
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long user_id;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String telephone;

    @OneToMany(mappedBy = "gestionnaire")
    private List<Flotte> flottes = new ArrayList<>();

    public User() {}

    // getters et setters 

        public Long getId() {
        return user_id;
    }

    public String getNom() {
        return name;
    }

    public void setNom(String nom) {
        this.name = nom;
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<Flotte> getFlottes() {
        return flottes;
    }

    public void setFlottes(List<Flotte> flottes) {
        this.flottes = flottes;
    }


}
