package traqueur_flotte.DTO;

import traqueur_flotte.model.TypeVehicule;

public class CreateVehiculeDTO {

    private String immatriculation;
    private TypeVehicule type;
    private Long flotteId;

    public CreateVehiculeDTO() {
    }

    public CreateVehiculeDTO(
            String immatriculation,
            TypeVehicule type,
            Long flotteId) {

        this.immatriculation = immatriculation;
        this.type = type;
        this.flotteId = flotteId;
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

    public Long getFlotteId() {
        return flotteId;
    }

    public void setFlotteId(Long flotteId) {
        this.flotteId = flotteId;
    }
}