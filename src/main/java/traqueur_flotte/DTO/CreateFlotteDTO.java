package traqueur_flotte.DTO;

public class CreateFlotteDTO {
    private String nom;

    private Double centreLatitude;
    private Double centreLongitude;
    private Double rayonMeters;

    public String getNom(){ return this.nom;}
    public Double getCentreLatitude(){return this.centreLatitude;}
    public Double getCentreLongitude(){return this.centreLongitude;}
    public Double getRayonMetres(){return this.rayonMeters;}

}
