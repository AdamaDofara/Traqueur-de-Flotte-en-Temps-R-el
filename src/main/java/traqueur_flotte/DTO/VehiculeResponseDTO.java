package traqueur_flotte.DTO;

import traqueur_flotte.model.TypeVehicule;

public class VehiculeResponseDTO {

    private Long id;
    private String immatriculation;
    private TypeVehicule type;
    private Boolean actif;
    private Long flotteId;
    private Long conducteurId;

    public VehiculeResponseDTO() {
    }

    public VehiculeResponseDTO(
            Long id,
            String immatriculation,
            TypeVehicule type,
            Boolean actif,
            Long flotteId,
            Long conducteurId) {

        this.id = id;
        this.immatriculation = immatriculation;
        this.type = type;
        this.actif = actif;
        this.flotteId = flotteId;
        this.conducteurId = conducteurId;
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

    public Long getFlotteId() {
        return flotteId;
    }

    public void setFlotteId(Long flotteId) {
        this.flotteId = flotteId;
    }

    public Long getConducteurId() {
        return conducteurId;
    }

    public void setConducteurId(Long conducteurId) {
        this.conducteurId = conducteurId;
    }
}