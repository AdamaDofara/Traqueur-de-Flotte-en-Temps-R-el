package traqueur_flotte.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte")
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime horodatageSortie;

    private LocalDateTime horodatageEntree;

    private Double latitudeSortie;

    private Double longitudeSortie;

    private Double latitudeEntree;

    private Double longitudeEntree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;


    public Alerte() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getHorodatageSortie() {
        return horodatageSortie;
    }

    public void setHorodatageSortie(LocalDateTime horodatageSortie) {
        this.horodatageSortie = horodatageSortie;
    }

    public LocalDateTime getHorodatageEntree() {
        return horodatageEntree;
    }

    public void setHorodatageEntree(LocalDateTime horodatageEntree) {
        this.horodatageEntree = horodatageEntree;
    }

    public Double getLatitudeSortie() {
        return latitudeSortie;
    }

    public void setLatitudeSortie(Double latitudeSortie) {
        this.latitudeSortie = latitudeSortie;
    }

    public Double getLongitudeSortie() {
        return longitudeSortie;
    }

    public void setLongitudeSortie(Double longitudeSortie) {
        this.longitudeSortie = longitudeSortie;
    }

    public Double getLatitudeEntree() {
        return latitudeEntree;
    }

    public void setLatitudeEntree(Double latitudeEntree) {
        this.latitudeEntree = latitudeEntree;
    }

    public Double getLongitudeEntree() {
        return longitudeEntree;
    }

    public void setLongitudeEntree(Double longitudeEntree) {
        this.longitudeEntree = longitudeEntree;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}
