package traqueur_flotte.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import traqueur_flotte.DTO.CreateFlotteDTO;
import traqueur_flotte.DTO.FlotteDTO;
import traqueur_flotte.DTO.FlotteDetailDTO;

import traqueur_flotte.services.FlotteService;

import java.util.List;



@RestController
@RequestMapping("/flottes")
public class FlotteController {

    private final FlotteService flotteService;

    public FlotteController(FlotteService flotteService) {
        this.flotteService = flotteService;
    }

    @PostMapping
    public FlotteDTO creerFlotte(
        @RequestBody CreateFlotteDTO dto,
        @RequestParam Long gestionnaireId) {

        return flotteService.creerFlotte(dto, gestionnaireId);
    }

    @GetMapping("/gestionnaire/{gestionnaireId}")
    public List<FlotteDTO> getFlottesByGestionnaire(
            @PathVariable Long gestionnaireId) {

        return flotteService
            .getFlottesByGestionnaire(gestionnaireId);
    }

    @GetMapping
    public List<FlotteDTO> getAllFlottes() {
        return flotteService.getAllFlottes();
    }

    @GetMapping("/{flotteId}")
    public FlotteDetailDTO getFlotteById(
            @PathVariable Long flotteId,
            @RequestParam Long utilisateurId) {

        return flotteService
            .getFlotteById(flotteId, utilisateurId);
    }
}