package traqueur_flotte.services;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import traqueur_flotte.DTO.ChangePasswordRequest;
import traqueur_flotte.DTO.ForgotPasswordRequest;
import traqueur_flotte.DTO.LoginRequest;
import traqueur_flotte.DTO.RegisterRequest;
import traqueur_flotte.DTO.ResetPasswordRequest;
import traqueur_flotte.DTO.UpdateProfilRequest;
import traqueur_flotte.DTO.UserDTO;
import traqueur_flotte.DTO.UserResponseDTO;
import traqueur_flotte.model.User;
import traqueur_flotte.repository.UserRepository;

/** Logique métier utilisateur: validation, encodage, authentification et persistance. */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Inscrit un utilisateur après validation et hashage BCrypt de son mot de passe. */
    @Transactional
    public UserResponseDTO inscrire(RegisterRequest request) {
        if (request == null) {
            throw badRequest("Les informations d'inscription sont obligatoires.");
        }
        String nom = required(request.nom(), "Le nom est obligatoire.");
        String prenom = required(request.prenom(), "Le prénom est obligatoire.");
        String email = normalizeEmail(request.email());
        validateEmail(email);
        validatePassword(request.motDePasse());
        if (request.role() == null) {
            throw badRequest("Le rôle est obligatoire.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserApiException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée.");
        }

        String telephone = normalizeTelephone(request.telephone());
        if (telephone != null && userRepository.existsByTelephone(telephone)) {
            throw new UserApiException(HttpStatus.CONFLICT, "Ce numéro de téléphone est déjà utilisé.");
        }

        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        user.setTelephone(telephone);
        user.setRole(request.role());
        user.setActif(true);
        return toResponse(userRepository.save(user));
    }

    /** Vérifie les identifiants et l'état du compte avant de retourner son profil. */
    @Transactional(readOnly = true)
    public UserResponseDTO connecter(LoginRequest request) {
        if (request == null) {
            throw new UserApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }
        String email = request.email() == null ? "" : normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect."));
        if (!user.isActif()) {
            throw new UserApiException(HttpStatus.FORBIDDEN, "Ce compte est désactivé.");
        }
        if (request.motDePasse() == null || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasse())) {
            throw new UserApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }
        return toResponse(user);
    }

    /** Retourne la liste historique des utilisateurs sans mot de passe. */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).toList();
    }

    /** Retourne le DTO historique d'un utilisateur par identifiant. */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return toDTO(getUser(id));
    }

    /** Retourne le profil public sans mot de passe ni token. */
    @Transactional(readOnly = true)
    public UserResponseDTO getProfilById(Long id) {
        return toResponse(getUser(id));
    }

    /** Met à jour les informations personnelles et contrôle l'unicité des coordonnées. */
    @Transactional
    public UserResponseDTO modifierProfil(Long id, UpdateProfilRequest request) {
        if (request == null) {
            throw badRequest("Les informations du profil sont obligatoires.");
        }
        User user = getUser(id);
        String nom = required(request.nom(), "Le nom est obligatoire.");
        String prenom = required(request.prenom(), "Le prénom est obligatoire.");
        String email = normalizeEmail(request.email());
        validateEmail(email);

        if (!email.equals(user.getEmail()) && userRepository.existsByEmailAndIdNot(email, id)) {
            throw new UserApiException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée.");
        }
        if (request.telephone() != null) {
            String telephone = normalizeTelephone(request.telephone());
            if (telephone != null && !telephone.equals(user.getTelephone())
                    && userRepository.existsByTelephoneAndIdNot(telephone, id)) {
                throw new UserApiException(HttpStatus.CONFLICT, "Ce numéro de téléphone est déjà utilisé.");
            }
            user.setTelephone(telephone);
        }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        return toResponse(userRepository.save(user));
    }

    /** Vérifie l'ancien secret et remplace le hash par celui du nouveau mot de passe. */
    @Transactional
    public void changerMotDePasse(Long id, ChangePasswordRequest request) {
        if (request == null) {
            throw badRequest("Les informations de changement de mot de passe sont obligatoires.");
        }
        User user = getUser(id);
        if (request.ancienMotDePasse() == null
                || !passwordEncoder.matches(request.ancienMotDePasse(), user.getMotDePasse())) {
            throw badRequest("L'ancien mot de passe est incorrect.");
        }
        validatePassword(request.nouveauMotDePasse());
        if (passwordEncoder.matches(request.nouveauMotDePasse(), user.getMotDePasse())) {
            throw badRequest("Le nouveau mot de passe doit être différent de l'ancien.");
        }
        user.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
        userRepository.save(user);
    }

    /** Génère un token de réinitialisation et le journalise pour transmission manuelle. */
    @Transactional
    public void demanderReinitialisation(ForgotPasswordRequest request) {
        if (request == null) {
            throw badRequest("L'adresse email est obligatoire.");
        }
        String email = normalizeEmail(request.email());
        validateEmail(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserApiException(HttpStatus.NOT_FOUND, "Aucun compte ne correspond à cet email."));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(Instant.now().plusSeconds(30 * 60));
        userRepository.save(user);
        LOGGER.info("Token de réinitialisation pour {} : {}", email, token);
    }

    /** Valide l'expiration du token, met à jour le hash puis consomme le token. */
    @Transactional
    public void reinitialiserMotDePasse(ResetPasswordRequest request) {
        if (request == null) {
            throw badRequest("Les informations de réinitialisation sont obligatoires.");
        }
        validatePassword(request.nouveauMotDePasse());
        if (request.token() == null || request.token().isBlank()) {
            throw badRequest("Le token est invalide ou expiré.");
        }
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> badRequest("Le token est invalide ou expiré."));
        if (user.getPasswordResetExpiresAt() == null || !user.getPasswordResetExpiresAt().isAfter(Instant.now())) {
            throw badRequest("Le token est invalide ou expiré.");
        }

        user.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }

    /** Supprime le compte; la relation JPA supprime également ses flottes associées. */
    @Transactional
    public void supprimerCompte(Long id) {
        userRepository.delete(getUser(id));
    }

    /** Adaptateur de compatibilité pour les anciens appelants du DTO UserDTO. */
    public UserDTO inscrire(UserDTO dto) {
        UserResponseDTO response = inscrire(new RegisterRequest(
                dto.getNom(), dto.getPrenom(), dto.getEmail(), dto.getMotDePasse(), dto.getRole(), dto.getTelephone()));
        return toLegacyDTO(response);
    }

    /** Adaptateur de compatibilité pour les anciens appelants du DTO UserDTO. */
    public UserDTO connecter(UserDTO dto) {
        UserResponseDTO response = connecter(new LoginRequest(dto.getEmail(), dto.getMotDePasse()));
        return toLegacyDTO(response);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw badRequest(message);
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw badRequest("L'adresse email est invalide.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw badRequest("Le mot de passe doit contenir au moins 8 caractères.");
        }
    }

    private String normalizeTelephone(String telephone) {
        return telephone == null || telephone.isBlank() ? null : telephone.trim();
    }

    private UserApiException badRequest(String message) {
        return new UserApiException(HttpStatus.BAD_REQUEST, message);
    }

    private UserDTO toLegacyDTO(UserResponseDTO response) {
        UserDTO dto = new UserDTO();
        dto.setId(response.id());
        dto.setNom(response.nom());
        dto.setPrenom(response.prenom());
        dto.setEmail(response.email());
        dto.setTelephone(response.telephone());
        dto.setRole(response.role());
        return dto;
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setRole(user.getRole());
        return dto;
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(), user.getNom(), user.getPrenom(), user.getEmail(),
                user.getTelephone(), user.getRole(), user.isActif());
    }
}
