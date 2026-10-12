package traqueur_flotte.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import traqueur_flotte.model.Vehicule;

public interface VehiculeRepository
        extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByFlotteId(Long flotteId);

    Optional<Vehicule> findByConducteurId(Long conducteurId);

    boolean existsByImmatriculation(String immatriculation);
}