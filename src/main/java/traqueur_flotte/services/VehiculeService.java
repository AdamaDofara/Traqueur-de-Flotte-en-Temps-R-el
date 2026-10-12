package traqueur_flotte.services;

import java.util.List;

import org.springframework.stereotype.Service;

import traqueur_flotte.DTO.CreateVehiculeDTO;
import traqueur_flotte.DTO.UpdateVehiculeDTO;
import traqueur_flotte.DTO.VehiculeResponseDTO;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.Role;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;
import traqueur_flotte.repository.VehiculeRepository;

@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final FlotteRepository flotteRepository;
    private final UserRepository userRepository;

    public VehiculeService(
            VehiculeRepository vehiculeRepository,
            FlotteRepository flotteRepository,
            UserRepository userRepository) {

        this.vehiculeRepository = vehiculeRepository;
        this.flotteRepository = flotteRepository;
        this.userRepository = userRepository;
    }

    // UC14 - Ajouter un véhicule à une flotte
    public VehiculeResponseDTO creerVehicule(CreateVehiculeDTO dto) {

        if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new RuntimeException("Cette immatriculation existe déjà.");
        }

        Flotte flotte = flotteRepository.findById(dto.getFlotteId())
                .orElseThrow(() ->
                        new RuntimeException("Flotte introuvable.")
                );

        Vehicule vehicule = new Vehicule();

        vehicule.setImmatriculation(dto.getImmatriculation());
        vehicule.setType(dto.getType());
        vehicule.setActif(false);
        vehicule.setFlotte(flotte);

        Vehicule vehiculeSauvegarde = vehiculeRepository.save(vehicule);

        return convertirEnDTO(vehiculeSauvegarde);
    }

    // UC15 - Consulter les véhicules d'une flotte
    public List<VehiculeResponseDTO> getVehiculesParFlotte(Long flotteId) {

        return vehiculeRepository.findByFlotteId(flotteId)
                .stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    // UC16 - Consulter le détail d'un véhicule
    public VehiculeResponseDTO getVehicule(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Véhicule introuvable.")
                );

        return convertirEnDTO(vehicule);
    }

    // UC17 - Modifier un véhicule
    public VehiculeResponseDTO modifierVehicule(
            Long id,
            UpdateVehiculeDTO dto) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Véhicule introuvable.")
                );

        if (!vehicule.getImmatriculation().equals(dto.getImmatriculation())
                && vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {

            throw new RuntimeException("Cette immatriculation existe déjà.");
        }

        vehicule.setImmatriculation(dto.getImmatriculation());
        vehicule.setType(dto.getType());

        Vehicule vehiculeSauvegarde = vehiculeRepository.save(vehicule);

        return convertirEnDTO(vehiculeSauvegarde);
    }

    // UC18 - Supprimer un véhicule
    public void supprimerVehicule(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Véhicule introuvable.")
                );

        vehiculeRepository.delete(vehicule);
    }

    // UC19 - Associer un conducteur
    public VehiculeResponseDTO associerConducteur(
            Long vehiculeId,
            Long conducteurId) {

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() ->
                        new RuntimeException("Véhicule introuvable.")
                );

        User conducteur = userRepository.findById(conducteurId)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable.")
                );

        if (conducteur.getRole() != Role.CONDUCTEUR) {
            throw new RuntimeException(
                    "Cet utilisateur n'a pas le rôle CONDUCTEUR."
            );
        }

        vehiculeRepository.findByConducteurId(conducteurId)
                .ifPresent(v -> {
                    throw new RuntimeException(
                            "Ce conducteur est déjà associé à un autre véhicule."
                    );
                });

        vehicule.setConducteur(conducteur);

        Vehicule vehiculeSauvegarde = vehiculeRepository.save(vehicule);

        return convertirEnDTO(vehiculeSauvegarde);
    }

    // UC20 - Dissocier un conducteur
    public VehiculeResponseDTO dissocierConducteur(Long vehiculeId) {

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() ->
                        new RuntimeException("Véhicule introuvable.")
                );

        vehicule.setConducteur(null);

        Vehicule vehiculeSauvegarde = vehiculeRepository.save(vehicule);

        return convertirEnDTO(vehiculeSauvegarde);
    }

    // Conversion Entity -> DTO
    private VehiculeResponseDTO convertirEnDTO(Vehicule vehicule) {

        Long flotteId = null;
        Long conducteurId = null;

        if (vehicule.getFlotte() != null) {
            flotteId = vehicule.getFlotte().getId();
        }

        if (vehicule.getConducteur() != null) {
            conducteurId = vehicule.getConducteur().getId();
        }

        return new VehiculeResponseDTO(
                vehicule.getId(),
                vehicule.getImmatriculation(),
                vehicule.getType(),
                vehicule.getActif(),
                flotteId,
                conducteurId
        );
    }
}