package traqueur_flotte.services;

import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.DTO.PositionGpsRequest;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.VehiculeRepository;

@Service
@Profile("websocket-demo")
public class PositionService {

    private final VehiculeRepository vehiculeRepository;

    public PositionService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    @Transactional(readOnly = true)
    public PositionGpsMessage preparerPosition(PositionGpsRequest demande) {
        if (demande.vehiculeId() == null || demande.vehiculeId() <= 0) {
            throw new IllegalArgumentException("L'identifiant du véhicule doit être positif.");
        }

        verifierCoordonnee(demande.latitude(), -90, 90, "latitude");
        verifierCoordonnee(demande.longitude(), -180, 180, "longitude");

        Vehicule vehicule = vehiculeRepository.findById(demande.vehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable."));

        Flotte flotte = vehicule.getFlotte();
        if (flotte == null) {
            throw new IllegalArgumentException("Ce véhicule n'est associé à aucune flotte.");
        }

        return new PositionGpsMessage(
                vehicule.getId(),
                flotte.getId(),
                demande.latitude(),
                demande.longitude(),
                Instant.now().toString()
        );
    }

    private void verifierCoordonnee(Double valeur, double min, double max, String nom) {
        if (valeur == null || !Double.isFinite(valeur) || valeur < min || valeur > max) {
            throw new IllegalArgumentException(
                    "La " + nom + " doit être comprise entre " + min + " et " + max + "."
            );
        }
    }
}