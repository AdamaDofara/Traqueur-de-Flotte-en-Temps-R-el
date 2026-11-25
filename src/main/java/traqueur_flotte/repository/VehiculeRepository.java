package traqueur_flotte.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import traqueur_flotte.model.Vehicule;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByFlotteId(Long flotteId);

    Optional<Vehicule> findByConducteurId(Long conducteurId);

    long countByFlotteIdAndActifTrue(Long flotteId);

    boolean existsByImmatriculation(String immatriculation);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vehicule> findVehiculeById(Long id);
}