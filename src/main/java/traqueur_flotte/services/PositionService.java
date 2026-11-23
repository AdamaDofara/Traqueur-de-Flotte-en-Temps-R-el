package traqueur_flotte.services;

import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.DTO.PositionGpsRequest;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.VehiculeRepository;

@Service
@Profile("websocket-demo")
public class PositionService {

    private final VehiculeRepository vehiculeRepository;
    private final ZoneFlotteService zoneFlotteService;

    public PositionService(
            VehiculeRepository vehiculeRepository,
            ZoneFlotteService zoneFlotteService
    ) {
        this.vehiculeRepository = vehiculeRepository;
        this.zoneFlotteService = zoneFlotteService;
    }

    @Transactional
    public PositionGpsMessage preparerPosition(PositionGpsRequest demande, Long conducteurId) {
        if (demande.vehiculeId() == null || demande.vehiculeId() <= 0) {
            throw new IllegalArgumentException("L'identifiant du véhicule doit être positif.");
        }

        verifierCoordonnee(demande.latitude(), -90, 90, "latitude");
        verifierCoordonnee(demande.longitude(), -180, 180, "longitude");

        Vehicule vehicule = vehiculeRepository.findVehiculeById(demande.vehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable."));

        User conducteur = vehicule.getConducteur();
        if (conducteur == null || !conducteur.getId().equals(conducteurId)) {
            throw new IllegalArgumentException("Ce véhicule n'est pas affecté à ton compte.");
        }

        Flotte flotte = vehicule.getFlotte();
        if (flotte == null) {
            throw new IllegalArgumentException("Ce véhicule n'est associé à aucune flotte.");
        }

        Instant recueA = Instant.now();
        zoneFlotteService.traiterPosition(
                vehicule, flotte, demande.latitude(), demande.longitude(), recueA
        );

        return new PositionGpsMessage(
                vehicule.getId(),
                flotte.getId(),
                demande.latitude(),
                demande.longitude(),
                recueA.toString()
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