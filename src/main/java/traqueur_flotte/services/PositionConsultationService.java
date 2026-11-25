package traqueur_flotte.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.model.Role;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;
import traqueur_flotte.repository.VehiculeRepository;

@Service
public class PositionConsultationService {

    private final VehiculeRepository vehiculeRepository;
    private final FlotteRepository flotteRepository;
    private final UserRepository userRepository;

    public PositionConsultationService(
            VehiculeRepository vehiculeRepository,
            FlotteRepository flotteRepository,
            UserRepository userRepository
    ) {
        this.vehiculeRepository = vehiculeRepository;
        this.flotteRepository = flotteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionGpsMessage> listerParFlotte(Long flotteId, Long utilisateurId) {
        User utilisateur = userRepository.findById(utilisateurId)
                .filter(User::isActif)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Compte inaccessible."
                ));

        verifierAccesFlotte(flotteId, utilisateur);

        return vehiculeRepository.findByFlotteId(flotteId)
                .stream()
                .filter(vehicule -> vehicule.getDerniereLatitude() != null
                        && vehicule.getDerniereLongitude() != null
                        && vehicule.getDernierePositionRecueA() != null)
                .sorted(Comparator.comparing(Vehicule::getId))
                .map(vehicule -> new PositionGpsMessage(
                        vehicule.getId(),
                        flotteId,
                        vehicule.getDerniereLatitude(),
                        vehicule.getDerniereLongitude(),
                        vehicule.getDernierePositionRecueA().toString()
                ))
                .toList();
    }

    private void verifierAccesFlotte(Long flotteId, User utilisateur) {
        if (utilisateur.getRole() == Role.ADMINISTRATEUR) {
            if (!flotteRepository.existsById(flotteId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flotte introuvable."
                );
            }
            return;
        }

        if (utilisateur.getRole() == Role.GESTIONNAIRE
                && flotteRepository.existsByIdAndGestionnaire_Id(
                        flotteId, utilisateur.getId()
                )) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Accès à cette flotte interdit."
        );
    }
}