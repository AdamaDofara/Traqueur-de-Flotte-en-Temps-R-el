package traqueur_flotte.DTO;

public class FlotteDTO {

    private Long id;
    private String nom;
    private Double centreLatitude;
    private Double centreLongitude;
    private Double rayonMetres;
    private Long gestionnaireId;

    public FlotteDTO() {
    }

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

    public Double getCentreLatitude() {
        return centreLatitude;
    }

    public void setCentreLatitude(Double centreLatitude) {
        this.centreLatitude = centreLatitude;
    }

    public Double getCentreLongitude() {
        return centreLongitude;
    }

    public void setCentreLongitude(Double centreLongitude) {
        this.centreLongitude = centreLongitude;
    }

    public Double getRayonMetres() {
        return rayonMetres;
    }

    public void setRayonMetres(Double rayonMetres) {
        this.rayonMetres = rayonMetres;
    }

    public Long getGestionnaireId() {
        return gestionnaireId;
    }

    public void setGestionnaireId(Long gestionnaireId) {
        this.gestionnaireId = gestionnaireId;
    }
}