package traqueur_flotte.DTO;

import traqueur_flotte.model.TypeVehicule;

public class UpdateVehiculeDTO {

    private String immatriculation;
    private TypeVehicule type;

    public UpdateVehiculeDTO() {
    }

    public UpdateVehiculeDTO(
            String immatriculation,
            TypeVehicule type) {

        this.immatriculation = immatriculation;
        this.type = type;
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
}