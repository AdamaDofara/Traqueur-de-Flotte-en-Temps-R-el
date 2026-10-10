package traqueur_flotte.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import traqueur_flotte.DTO.UserDTO;
import traqueur_flotte.model.User;
import traqueur_flotte.repository.UserRepository;

/**
 * Couche Service : contient la logique métier.
 * C'est la seule couche qui parle au Repository.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    // Injection par constructeur (recommandée, @Autowired optionnel ici)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Inscrire un nouvel utilisateur.
     */
    public UserDTO inscrire(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Erreur : L'adresse email est déjà utilisée.");
        }
        User user = toEntity(dto);
        user.setActif(true);
        User nouvelUser = userRepository.save(user);
        return toDTO(nouvelUser);
    }

    /**
     * Récupérer tous les utilisateurs.
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ---------- Conversions DTO <-> Entité ----------

    private User toEntity(UserDTO dto) {
        User user = new User();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setMotDePasse(dto.getMotDePasse());
        user.setTelephone(dto.getTelephone());
        user.setRole(dto.getRole());
        return user;
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        // On ne renvoie jamais le mot de passe dans une réponse
        dto.setTelephone(user.getTelephone());
        dto.setRole(user.getRole());
        return dto;
    }
}
