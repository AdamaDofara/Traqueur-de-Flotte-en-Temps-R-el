package traqueur_flotte.repository;

import java.util.List;
import java.util.Optional;

import traqueur_flotte.model.TypeVehicule;
import traqueur_flotte.model.Vehicule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long>{

    List<Vehicule> findByType(TypeVehicule  type);
    List<Vehicule> findByActif(boolean actif);
    List<Vehicule> findByFlotteId(Long flotteId);
    Optional<Vehicule> findByConducteurId(Long conducteurId);

}