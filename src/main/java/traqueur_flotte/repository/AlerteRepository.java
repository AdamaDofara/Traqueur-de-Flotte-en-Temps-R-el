package traqueur_flotte.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import traqueur_flotte.model.Alerte;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByVehiculeId(Long vehiculeId);

    Optional<Alerte> findFirstByVehiculeIdAndHorodatageEntreeIsNullOrderByIdDesc(
            Long vehiculeId
    );

    List<Alerte> findByVehicule_Flotte_IdOrderByHorodatageSortieDescIdDesc(
            Long flotteId
    );
}