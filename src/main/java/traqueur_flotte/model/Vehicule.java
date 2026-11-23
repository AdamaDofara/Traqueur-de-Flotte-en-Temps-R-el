package traqueur_flotte.model;

import java.time.Instant;
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

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
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

    @Column(name = "derniere_latitude")
    private Double derniereLatitude;

    @Column(name = "derniere_longitude")
    private Double derniereLongitude;

    @Column(name = "derniere_position_recue_a")
    private Instant dernierePositionRecueA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flotte_id")
    private Flotte flotte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", unique = true)
    private User conducteur;

    @OneToMany(mappedBy = "vehicule")
    private List<Alerte> alertes = new ArrayList<>();

    public Vehicule() {
    }

    public Vehicule(String immatriculation, TypeVehicule type, Boolean actif) {
        this.immatriculation = immatriculation;
        this.type = type;
        this.actif = actif;
    }

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

    public Double getDerniereLatitude() {
        return derniereLatitude;
    }

    public Double getDerniereLongitude() {
        return derniereLongitude;
    }

    public Instant getDernierePositionRecueA() {
        return dernierePositionRecueA;
    }

    public void enregistrerPosition(double latitude, double longitude, Instant recueA) {
        this.derniereLatitude = latitude;
        this.derniereLongitude = longitude;
        this.dernierePositionRecueA = recueA;
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