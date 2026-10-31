package traqueur_flotte.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.model.Role;

import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;
import traqueur_flotte.repository.VehiculeRepository;

import traqueur_flotte.DTO.CreateFlotteDTO;
import traqueur_flotte.DTO.FlotteDTO;
import traqueur_flotte.DTO.FlotteDetailDTO;
import traqueur_flotte.DTO.VehiculeDTO;


@Service
public class FlotteService {

    private final FlotteRepository flotteRepository;
    private final UserRepository userRepository;
    private final VehiculeRepository vehiculeRepository;


    // Constructeur
    public FlotteService(
            FlotteRepository flotteRepository,
            UserRepository userRepository,
            VehiculeRepository vehiculeRepository) {

        this.flotteRepository = flotteRepository;
        this.userRepository = userRepository;
        this.vehiculeRepository = vehiculeRepository;
    }


    // =========================================================
    // UC09 - Créer une flotte
    // =========================================================

    public FlotteDTO creerFlotte(
            CreateFlotteDTO dto,
            Long gestionnaireId) {

        // Le nom est obligatoire
        if (dto.getNom() == null || dto.getNom().isBlank()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Une flotte doit obligatoirement avoir un nom"
            );
        }


        // Vérifier que l'utilisateur existe
        User gestionnaire = userRepository
            .findById(gestionnaireId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Gestionnaire introuvable"
                )
            );


        // Vérifier que l'utilisateur est bien gestionnaire
        if (gestionnaire.getRole() != Role.GESTIONNAIRE) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Seul un gestionnaire peut créer une flotte"
            );
        }


        // Maximum 2 flottes par gestionnaire
        if (flotteRepository.countByGestionnaireId(gestionnaireId) >= 2) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Un gestionnaire ne peut pas posséder plus de 2 flottes"
            );
        }


        // Un gestionnaire ne peut pas avoir deux flottes du même nom
        if (flotteRepository.existsByNomAndGestionnaireId(
                dto.getNom(),
                gestionnaireId)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Une flotte avec ce nom existe déjà"
            );
        }


        // Création de la flotte
        Flotte flotte = new Flotte();

        flotte.setNom(dto.getNom());
        flotte.setCentreLatitude(dto.getCentreLatitude());
        flotte.setCentreLongitude(dto.getCentreLongitude());
        flotte.setRayonMetres(dto.getRayonMetres());
        flotte.setGestionnaire(gestionnaire);


        // Sauvegarde en base de données
        Flotte flotteSauvegardee =
            flotteRepository.save(flotte);


        // On retourne un DTO et non directement l'entité
        return convertirEnDTO(flotteSauvegardee);
    }


    // =========================================================
    // UC10 - Récupérer les flottes d'un gestionnaire
    // =========================================================

    public List<FlotteDTO> getFlottesByGestionnaire(
            Long gestionnaireId) {

        // Vérifier que le gestionnaire existe
        User gestionnaire = userRepository
            .findById(gestionnaireId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Gestionnaire introuvable"
                )
            );


        // Vérifier son rôle
        if (gestionnaire.getRole() != Role.GESTIONNAIRE) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cet utilisateur n'est pas un gestionnaire"
            );
        }


        List<Flotte> flottes =
            flotteRepository.findByGestionnaireId(gestionnaireId);


        return flottes.stream()
            .map(this::convertirEnDTO)
            .toList();
    }


    // =========================================================
    // UC10 - L'administrateur peut voir toutes les flottes
    // =========================================================

    public List<FlotteDTO> getAllFlottes() {

        return flotteRepository
            .findAll()
            .stream()
            .map(this::convertirEnDTO)
            .toList();
    }


    // =========================================================
    // UC11 - Récupérer le détail d'une flotte
    // =========================================================

    public FlotteDetailDTO getFlotteById(
            Long flotteId,
            Long utilisateurId) {


        // Vérifier que la flotte existe
        Flotte flotte = flotteRepository
            .findById(flotteId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Flotte introuvable"
                )
            );


        // Vérifier que l'utilisateur existe
        User utilisateur = userRepository
            .findById(utilisateurId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur introuvable"
                )
            );


        /*
         * Un administrateur peut consulter toutes les flottes.
         *
         * Un gestionnaire ne peut consulter que ses propres flottes.
         */
        if (utilisateur.getRole() != Role.ADMINISTRATEUR) {

            if (!flotte.getGestionnaire()
                       .getId()
                       .equals(utilisateurId)) {

                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas autorisé à consulter cette flotte"
                );
            }
        }


        // Récupérer tous les véhicules de la flotte
        List<Vehicule> vehicules =
            vehiculeRepository.findByFlotteId(flotteId);


        // Transformer les Vehicule en VehiculeDTO
        List<VehiculeDTO> vehiculesDTO =
            vehicules.stream()
                .map(this::convertirVehiculeEnDTO)
                .toList();


        // Compter les véhicules actifs de la flotte
        long nombreActifs =
            vehiculeRepository
                .countByFlotteIdAndActifTrue(flotteId);


        // Construction du DTO détaillé
        FlotteDetailDTO dto = new FlotteDetailDTO();

        dto.setId(flotte.getId());
        dto.setNom(flotte.getNom());

        dto.setCentreLatitude(
            flotte.getCentreLatitude()
        );

        dto.setCentreLongitude(
            flotte.getCentreLongitude()
        );

        dto.setRayonMetres(
            flotte.getRayonMetres()
        );

        dto.setGestionnaireId(
            flotte.getGestionnaire().getId()
        );

        dto.setVehicules(vehiculesDTO);

        dto.setNombreVehiculesActifs(
            nombreActifs
        );


        return dto;
    }


    // =========================================================
    // Conversion Flotte -> FlotteDTO
    // =========================================================

    private FlotteDTO convertirEnDTO(Flotte flotte) {

        FlotteDTO dto = new FlotteDTO();

        dto.setId(flotte.getId());
        dto.setNom(flotte.getNom());

        dto.setCentreLatitude(
            flotte.getCentreLatitude()
        );

        dto.setCentreLongitude(
            flotte.getCentreLongitude()
        );

        dto.setRayonMetres(
            flotte.getRayonMetres()
        );


        if (flotte.getGestionnaire() != null) {

            dto.setGestionnaireId(
                flotte.getGestionnaire().getId()
            );
        }


        return dto;
    }


    // =========================================================
    // Conversion Vehicule -> VehiculeDTO
    // =========================================================

    private VehiculeDTO convertirVehiculeEnDTO(
            Vehicule vehicule) {

        VehiculeDTO dto = new VehiculeDTO();

        dto.setId(
            vehicule.getId()
        );

        dto.setImmatriculation(
            vehicule.getImmatriculation()
        );

        dto.setType(
            vehicule.getType()
        );

        dto.setActif(
            vehicule.getActif()
        );


        return dto;
    }
}