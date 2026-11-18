package traqueur_flotte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import traqueur_flotte.DTO.AssocierConducteurDTO;
import traqueur_flotte.DTO.CreateVehiculeDTO;
import traqueur_flotte.DTO.UpdateVehiculeDTO;
import traqueur_flotte.DTO.VehiculeResponseDTO;
import traqueur_flotte.services.VehiculeService;

@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    // UC14 - Ajouter un véhicule
    @PostMapping
    public ResponseEntity<VehiculeResponseDTO> creerVehicule(
            @RequestBody CreateVehiculeDTO dto) {

        VehiculeResponseDTO vehicule =
                vehiculeService.creerVehicule(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicule);
    }

    // UC15 - Lister les véhicules d'une flotte
    @GetMapping("/flotte/{flotteId}")
    public ResponseEntity<List<VehiculeResponseDTO>>
            getVehiculesParFlotte(
                    @PathVariable Long flotteId) {

        List<VehiculeResponseDTO> vehicules =
                vehiculeService.getVehiculesParFlotte(flotteId);

        return ResponseEntity.ok(vehicules);
    }

    // UC16 - Consulter le détail d'un véhicule
    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO>
            getVehicule(@PathVariable Long id) {

        VehiculeResponseDTO vehicule =
                vehiculeService.getVehicule(id);

        return ResponseEntity.ok(vehicule);
    }

    // UC17 - Modifier un véhicule
    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO>
            modifierVehicule(
                    @PathVariable Long id,
                    @RequestBody UpdateVehiculeDTO dto) {

        VehiculeResponseDTO vehicule =
                vehiculeService.modifierVehicule(id, dto);

        return ResponseEntity.ok(vehicule);
    }

    // UC18 - Supprimer un véhicule
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            supprimerVehicule(@PathVariable Long id) {

        vehiculeService.supprimerVehicule(id);

        return ResponseEntity.noContent().build();
    }

    // UC19 - Associer un conducteur
    @PutMapping("/{id}/conducteur")
    public ResponseEntity<VehiculeResponseDTO>
            associerConducteur(
                    @PathVariable Long id,
                    @RequestBody AssocierConducteurDTO dto) {

        VehiculeResponseDTO vehicule =
                vehiculeService.associerConducteur(
                        id,
                        dto.getConducteurId()
                );

        return ResponseEntity.ok(vehicule);
    }

    // UC20 - Dissocier un conducteur
    @DeleteMapping("/{id}/conducteur")
    public ResponseEntity<VehiculeResponseDTO>
            dissocierConducteur(
                    @PathVariable Long id) {

        VehiculeResponseDTO vehicule =
                vehiculeService.dissocierConducteur(id);

        return ResponseEntity.ok(vehicule);
    }
}