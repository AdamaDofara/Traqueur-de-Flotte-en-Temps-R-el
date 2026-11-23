package traqueur_flotte.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import traqueur_flotte.DTO.AlerteResponseDTO;
import traqueur_flotte.model.Alerte;
import traqueur_flotte.model.Role;
import traqueur_flotte.model.User;
import traqueur_flotte.repository.AlerteRepository;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;

@Service
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final FlotteRepository flotteRepository;
    private final UserRepository userRepository;

    public AlerteService(
            AlerteRepository alerteRepository,
            FlotteRepository flotteRepository,
            UserRepository userRepository
    ) {
        this.alerteRepository = alerteRepository;
        this.flotteRepository = flotteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AlerteResponseDTO> listerParFlotte(Long flotteId, Long utilisateurId) {
        User utilisateur = userRepository.findById(utilisateurId)
                .filter(User::isActif)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Compte inaccessible."
                ));

        verifierAccesFlotte(flotteId, utilisateur);

        return alerteRepository
                .findByVehicule_Flotte_IdOrderByHorodatageSortieDescIdDesc(flotteId)
                .stream()
                .map(this::versDto)
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

    private AlerteResponseDTO versDto(Alerte alerte) {
        return new AlerteResponseDTO(
                alerte.getId(),
                alerte.getVehicule().getId(),
                alerte.getHorodatageSortie(),
                alerte.getLatitudeSortie(),
                alerte.getLongitudeSortie(),
                alerte.getHorodatageEntree(),
                alerte.getLatitudeEntree(),
                alerte.getLongitudeEntree()
        );
    }
}