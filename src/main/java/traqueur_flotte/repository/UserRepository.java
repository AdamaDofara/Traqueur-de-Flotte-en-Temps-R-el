package traqueur_flotte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import traqueur_flotte.model.User;

/**
 * Couche Repository : accès à la base de données uniquement.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
