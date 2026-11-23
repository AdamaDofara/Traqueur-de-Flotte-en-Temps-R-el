package traqueur_flotte.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import traqueur_flotte.model.Alerte;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.AlerteRepository;

@Service
@Profile("websocket-demo")
public class ZoneFlotteService {

    private static final double RAYON_TERRE_METRES = 6_371_000;

    private final AlerteRepository alerteRepository;

    public ZoneFlotteService(AlerteRepository alerteRepository) {
        this.alerteRepository = alerteRepository;
    }

    public void traiterPosition(
            Vehicule vehicule,
            Flotte flotte,
            double latitude,
            double longitude,
            Instant recueA
    ) {
        Double centreLatitude = flotte.getCentreLatitude();
        Double centreLongitude = flotte.getCentreLongitude();
        Double rayon = flotte.getRayonMetres();

        if (!dansIntervalle(centreLatitude, -90, 90)
                || !dansIntervalle(centreLongitude, -180, 180)
                || rayon == null || !Double.isFinite(rayon) || rayon <= 0) {
            return;
        }

        double distance = distanceMetres(
                centreLatitude, centreLongitude, latitude, longitude
        );
        boolean horsZone = distance > rayon;

        Optional<Alerte> alerteOuverte = alerteRepository
                .findFirstByVehiculeIdAndHorodatageEntreeIsNullOrderByIdDesc(vehicule.getId());

        LocalDateTime heureUtc = LocalDateTime.ofInstant(recueA, ZoneOffset.UTC);

        if (horsZone && alerteOuverte.isEmpty()) {
            Alerte alerte = new Alerte();
            alerte.setVehicule(vehicule);
            alerte.setHorodatageSortie(heureUtc);
            alerte.setLatitudeSortie(latitude);
            alerte.setLongitudeSortie(longitude);
            alerteRepository.save(alerte);
        } else if (!horsZone && alerteOuverte.isPresent()) {
            Alerte alerte = alerteOuverte.get();
            alerte.setHorodatageEntree(heureUtc);
            alerte.setLatitudeEntree(latitude);
            alerte.setLongitudeEntree(longitude);
            alerteRepository.save(alerte);
        }
    }

    private boolean dansIntervalle(Double valeur, double min, double max) {
        return valeur != null && Double.isFinite(valeur) && valeur >= min && valeur <= max;
    }

    private double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
        double deltaLatitude = Math.toRadians(lat2 - lat1);
        double deltaLongitude = Math.toRadians(lon2 - lon1);

        double sinusLatitude = Math.sin(deltaLatitude / 2);
        double sinusLongitude = Math.sin(deltaLongitude / 2);

        double a = sinusLatitude * sinusLatitude
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * sinusLongitude * sinusLongitude;

        return 2 * RAYON_TERRE_METRES * Math.asin(Math.sqrt(Math.min(1.0, a)));
    }
}