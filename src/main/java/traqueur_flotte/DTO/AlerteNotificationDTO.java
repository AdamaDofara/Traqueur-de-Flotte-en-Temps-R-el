package traqueur_flotte.DTO;

public record AlerteNotificationDTO(
        Long alerteId,
        Long flotteId,
        Long vehiculeId,
        Type type,
        double latitude,
        double longitude,
        String horodatage
) {
    public enum Type {
        OUVERTE,
        CLOTUREE
    }
}