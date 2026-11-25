package traqueur_flotte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import traqueur_flotte.model.Flotte;

public interface FlotteRepository extends JpaRepository<Flotte, Long> {

    List<Flotte> findByGestionnaireId(Long gestionnaireId);

    boolean existsByIdAndGestionnaire_Id(Long flotteId, Long gestionnaireId);

    boolean existsByNomAndGestionnaireId(String nom, Long gestionnaireId);

    int countByGestionnaireId(Long gestionnaireId);
}