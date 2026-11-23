package traqueur_flotte.DTO;

import java.time.LocalDateTime;

public record AlerteResponseDTO(
        Long id,
        Long vehiculeId,
        LocalDateTime horodatageSortie,
        Double latitudeSortie,
        Double longitudeSortie,
        LocalDateTime horodatageEntree,
        Double latitudeEntree,
        Double longitudeEntree
) {
}