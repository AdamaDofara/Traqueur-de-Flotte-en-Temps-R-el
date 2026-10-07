package traqueur_flotte.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicule_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String immatriculation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeVehicule type;

    @Column(nullable = false)
    private Boolean actif = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flotte_id")
    private Flotte flotte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", unique = true)
    private User conducteur;

    @OneToMany(mappedBy = "vehicule")
    private List<Alerte> alertes = new ArrayList<>();


    // Constructeur vide requis par JPA
    public Vehicule() {
    }

    // Constructeur pratique
    public Vehicule(
            String immatriculation,
            TypeVehicule type,
            Boolean actif) {

        this.immatriculation = immatriculation;
        this.type = type;
        this.actif = actif;
    }


    // =========================
    // Getters et Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public TypeVehicule getType() {
        return type;
    }

    public void setType(TypeVehicule type) {
        this.type = type;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Flotte getFlotte() {
        return flotte;
    }

    public void setFlotte(Flotte flotte) {
        this.flotte = flotte;
    }

    public User getConducteur() {
        return conducteur;
    }

    public void setConducteur(User conducteur) {
        this.conducteur = conducteur;
    }

    public List<Alerte> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<Alerte> alertes) {
        this.alertes = alertes;
    }


    // =========================
    // Méthodes métier simples
    // =========================

    public void associerConducteur(User conducteur) {
        this.conducteur = conducteur;
    }

    public void dissocierConducteur() {
        this.conducteur = null;
    }


    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", immatriculation='" + immatriculation + '\'' +
                ", type=" + type +
                ", actif=" + actif +
                '}';
    }
}