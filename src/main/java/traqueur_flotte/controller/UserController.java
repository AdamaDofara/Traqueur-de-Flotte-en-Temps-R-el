package traqueur_flotte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import traqueur_flotte.DTO.UserDTO;
import traqueur_flotte.services.UserService;

/**
 * Couche Controller : gère uniquement les requêtes/réponses HTTP.
 * Elle ne parle jamais au Repository directement, uniquement au Service.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 1. S'inscrire / Créer un nouveau compte
     * POST /api/users/inscription
     */
    @PostMapping("/inscription")
    public ResponseEntity<?> inscrire(@RequestBody UserDTO userDTO) {
        try {
            UserDTO nouvelUser = userService.inscrire(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelUser);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 2. Lister tous les utilisateurs
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
