package traqueur_flotte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import traqueur_flotte.model.Alerte;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByVehiculeId(Long vehiculeId);
}
