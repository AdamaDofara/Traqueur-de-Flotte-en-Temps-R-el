package traqueur_flotte.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;

@Entity
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicule_id")
    private Long id;

    private String immatriculation;

    @Enumerated(EnumType.STRING)
    private TypeVehicule type;

    private Boolean actif;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flotte_id", nullable = true)
    private Flotte flotte;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", unique = true)
    private User conducteur; 

    // Constructeur vide requis par JPA
    public Vehicule() {
    }

    // Constructeur pratique
    public Vehicule(String immatriculation, TypeVehicule type, Boolean actif) {
        this.immatriculation = immatriculation;
        this.type = type;
        this.actif = actif;
    }

    // Getters et setters

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