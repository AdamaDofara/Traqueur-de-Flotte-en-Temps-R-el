package traqueur_flotte.DTO;

import traqueur_flotte.model.TypeVehicule;

public class VehiculeDTO {

    private Long id;
    private String immatriculation;
    private TypeVehicule type;
    private boolean actif;

    public VehiculeDTO() {
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

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}