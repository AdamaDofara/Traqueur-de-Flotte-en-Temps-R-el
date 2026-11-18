package traqueur_flotte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import traqueur_flotte.model.User;

/** Accès Spring Data JPA aux utilisateurs et aux jetons de réinitialisation. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Indique si l'email est déjà associé à un compte. */
    boolean existsByEmail(String email);

    /** Recherche un doublon email en excluant l'utilisateur courant. */
    boolean existsByEmailAndIdNot(String email, Long id);

    /** Indique si le téléphone facultatif est déjà utilisé. */
    boolean existsByTelephone(String telephone);

    /** Recherche un doublon téléphone en excluant l'utilisateur courant. */
    boolean existsByTelephoneAndIdNot(String telephone, Long id);

    /** Recherche un utilisateur par son adresse email normalisée. */
    Optional<User> findByEmail(String email);

    /** Recherche le compte associé à un token de réinitialisation. */
    Optional<User> findByPasswordResetToken(String passwordResetToken);
}
