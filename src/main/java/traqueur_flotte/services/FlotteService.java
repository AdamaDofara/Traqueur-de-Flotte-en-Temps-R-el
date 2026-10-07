package traqueur_flotte.services; 

import java.util.List;
import java.util.Optional; 

import org.springframework.stereotype.Service;

import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.User;

import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;

import traqueur_flotte.DTO.CreateFlotteDTO;


@Service 
public class FlotteService{

    private final FlotteRepository flotteRepository;
    private final UserRepository userRepository;

    public FlotteService(FlotteRepository flotteRepository, UserRepository userRepository){

        this.flotteRepository = flotteRepository; 
        this.userRepository = userRepository; 
    }

    public Flotte creerFlotte(CreateFlotteDTO dto, Long gestionnaireId){

            if(dto.getNom() == null || dto.getNom().isBlank()){
                throw new IllegalArgumentException(
                    "Une flotte doit obligatoirement avoir un nom"
                );
            }

            User gestionnaire = userRepository.findById(gestionnaireId)
                .orElseThrow(() -> new RuntimeException("Gestionnaire introuvable")
            );

            if (flotteRepository.countByGestionnaireId(gestionnaireId) >= 2) {
                throw new IllegalArgumentException(
                    "Un gestionnaire ne peut pas posséder plus de 2 flottes"
                );
            }

            if (flotteRepository.existsByNomAndGestionnaireId(dto.getNom(),gestionnaireId)){

                    throw new IllegalArgumentException(
                        "Une flotte avec ce nom existe déjà"
                    );
                    }


            Flotte flotte = new Flotte();

            flotte.setNom(dto.getNom());
            flotte.setCentreLatitude(dto.getCentreLatitude());
            flotte.setCentreLongitude(dto.getCentreLongitude());
            flotte.setRayonMetres(dto.getRayonMetres());
            flotte.setGestionnaire(gestionnaire);

            return flotteRepository.save(flotte);
    }

}