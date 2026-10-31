package traqueur_flotte.DTO;

public class CreateFlotteDTO {

    private String nom;
    private Double centreLatitude;
    private Double centreLongitude;
    private Double rayonMetres;

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
}