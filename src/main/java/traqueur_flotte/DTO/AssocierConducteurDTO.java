package traqueur_flotte.DTO;

public class AssocierConducteurDTO {

    private Long conducteurId;

    public AssocierConducteurDTO() {
    }

    public AssocierConducteurDTO(Long conducteurId) {
        this.conducteurId = conducteurId;
    }

    public Long getConducteurId() {
        return conducteurId;
    }

    public void setConducteurId(Long conducteurId) {
        this.conducteurId = conducteurId;
    }
}