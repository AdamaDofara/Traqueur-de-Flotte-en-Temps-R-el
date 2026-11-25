package traqueur_flotte.DTO;

public record PositionGpsRequest(
        Long vehiculeId,
        Double latitude,
        Double longitude
) {
}