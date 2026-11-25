package traqueur_flotte.DTO;

public record PositionGpsMessage(
        Long vehiculeId,
        Long flotteId,
        double latitude,
        double longitude,
        String recueA
) {
}