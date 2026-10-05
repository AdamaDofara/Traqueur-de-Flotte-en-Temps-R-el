package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import model.User;


public class UserController {

	@Autowired
    private UserRepository UserRepository;
	

    /**
     * 1. S'inscrire / Créer un nouveau compte
     * POST /api/Users/inscription
     */
    @PostMapping("/inscription")
    public ResponseEntity<?> inscrire(@RequestBody User User) {
        if (UserRepository.existsByEmail(User.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur : L'adresse email est déjà utilisée.");
        }
        User.setActif(true);
        User nouvelUser = UserRepository.save(User);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouvelUser);
    }
}
