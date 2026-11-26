package traqueur_flotte.DTO;

public record AlerteNotificationDTO(
        Long alerteId,
        Long flotteId,
        Long vehiculeId,
        String immatriculation,
        Type type,
        double latitude,
        double longitude,
        String horodatage
) {
    public enum Type {
        OUVERTE,
        RAPPEL,
        CLOTUREE
    }
}