package traqueur_flotte.model;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "flottes")	
public class Flotte {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flotte_id")
    private Long id;

    @Column(nullable = false)
    private String nom;

    private Double centreLatitude;
    private Double centreLongitude;
    private Double rayonMeters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id")
    private User gestionnaire;

    @OneToMany(mappedBy = "flotte", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehicule> vehicules = new ArrayList<>();

    // Constructeurs
    public Flotte() {}

    public Flotte(String nom, Double centreLatitude, Double centreLongitude, Double rayonMeters) {
        this.nom = nom;
        this.centreLatitude = centreLatitude;
        this.centreLongitude = centreLongitude;
        this.rayonMeters = rayonMeters;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Double getCentreLatitude() { return centreLatitude; }
    public void setCentreLatitude(Double centreLatitude) { this.centreLatitude = centreLatitude; }

    public Double getCentreLongitude() { return centreLongitude; }
    public void setCentreLongitude(Double centreLongitude) { this.centreLongitude = centreLongitude; }

    public Double getRayonMeters() { return rayonMeters; }
    public void setRayonMeters(Double rayonMeters) { this.rayonMeters = rayonMeters; }

    public User getGestionnaire() { return gestionnaire; }
    public void setGestionnaire(User gestionnaire) { this.gestionnaire = gestionnaire; }

    public List<Vehicule> getVehicules() { return vehicules; }
    public void setVehicules(List<Vehicule> vehicules) { this.vehicules = vehicules; }
}
