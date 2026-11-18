package traqueur_flotte.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import traqueur_flotte.DTO.ApiMessageResponse;
import traqueur_flotte.DTO.ApiErrorResponse;
import traqueur_flotte.DTO.AuthResponse;
import traqueur_flotte.DTO.ChangePasswordRequest;
import traqueur_flotte.DTO.ForgotPasswordRequest;
import traqueur_flotte.DTO.LoginRequest;
import traqueur_flotte.DTO.RegisterRequest;
import traqueur_flotte.DTO.ResetPasswordRequest;
import traqueur_flotte.DTO.UpdateProfilRequest;
import traqueur_flotte.DTO.UserDTO;
import traqueur_flotte.DTO.UserResponseDTO;
import traqueur_flotte.security.JwtService;
import traqueur_flotte.services.UserApiException;
import traqueur_flotte.services.UserService;

/**
 * Couche Controller : gère uniquement les requêtes/réponses HTTP.
 * Elle ne parle jamais au Repository directement, uniquement au Service.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs et authentification", description = "Inscription, JWT, profil et gestion des mots de passe.")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    /** Construit le contrôleur avec les services utilisateur et JWT. */
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

        /** Crée un compte utilisateur et retourne son profil sans secret. */
        @Operation(summary = "UC01 - Inscrire un utilisateur", description = "Le mot de passe doit contenir au moins 8 caractères. Le téléphone est facultatif.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Informations du nouveau compte", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterRequest.class), examples = @ExampleObject(name = "Inscription", value = "{\"nom\":\"Martin\",\"prenom\":\"Lea\",\"email\":\"lea.martin@example.com\",\"motDePasse\":\"Secret123\",\"role\":\"GESTIONNAIRE\",\"telephone\":\"+33123456789\"}"))))
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compte créé", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Champ manquant ou invalide", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou téléphone déjà utilisé", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
    @PostMapping("/inscription")
    public ResponseEntity<UserResponseDTO> inscrire(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.inscrire(request));
    }

        /** Authentifie l'utilisateur et retourne un access token JWT. */
        @Operation(summary = "UC02 - Se connecter", description = "La réponse contient un access token HS256 valable par défaut 15 minutes. L'envoyer dans Authorization: Bearer <token>.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Identifiants", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(name = "Connexion", value = "{\"email\":\"lea.martin@example.com\",\"motDePasse\":\"Secret123\"}"))))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie; le JWT est retourné", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide"),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Compte désactivé", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
    @PostMapping("/connexion")
    public ResponseEntity<AuthResponse> connecter(@RequestBody LoginRequest request) {
        UserResponseDTO user = userService.connecter(request);
        JwtService.IssuedToken token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(
                token.value(), "Bearer", token.expiresInSeconds(), user, "Connexion réussie."));
    }

    /** Termine la session côté client par suppression locale du token. */
    @Operation(summary = "UC03 - Se déconnecter", description = "Un JWT déjà émis reste valide jusqu'à expiration; le client doit supprimer son access token.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Session terminée")})
    @PostMapping("/deconnexion")
    public ResponseEntity<Void> deconnecter() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

        /** Génère et journalise un token temporaire pour le compte associé à l'email. */
        @Operation(summary = "UC07 - Demander une réinitialisation", description = "Le token unique expire après 30 minutes et est écrit dans les logs applicatifs.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email du compte", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ForgotPasswordRequest.class), examples = @ExampleObject(value = "{\"email\":\"lea.martin@example.com\"}"))))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token généré et journalisé", content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email invalide", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<ApiMessageResponse> motDePasseOublie(@RequestBody ForgotPasswordRequest request) {
        userService.demanderReinitialisation(request);
        return ResponseEntity.ok(new ApiMessageResponse("Un token a été généré et journalisé."));
    }

        /** Valide le token temporaire et remplace le mot de passe par sa version hashée. */
        @Operation(summary = "UC07 - Réinitialiser le mot de passe", description = "Le token doit être valide et non expiré; le nouveau mot de passe doit avoir au moins 8 caractères.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Token et nouveau mot de passe", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResetPasswordRequest.class), examples = @ExampleObject(value = "{\"token\":\"token-recu-dans-les-logs\",\"nouveauMotDePasse\":\"NouveauSecret123\"}"))))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé", content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token expiré/invalide ou mot de passe non conforme", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<ApiMessageResponse> reinitialiserMotDePasse(@RequestBody ResetPasswordRequest request) {
        userService.reinitialiserMotDePasse(request);
        return ResponseEntity.ok(new ApiMessageResponse("Le mot de passe a été réinitialisé."));
    }

    /** Retourne la liste historique des utilisateurs, sans valeurs de mot de passe. */
    @Operation(summary = "Lister les utilisateurs", description = "Route historique conservée pour compatibilité; le DTO de réponse ne contient pas de mot de passe.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Liste retournée")})
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

        /** Retourne le profil de l'utilisateur connecté. */
        @Operation(summary = "UC04 - Consulter son profil", description = "L'identifiant de la ressource doit correspondre au subject du JWT.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil retourné", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Identifiant invalide"),
            @ApiResponse(responseCode = "401", description = "Authentification requise", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès à un autre profil interdit", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
    @GetMapping("/{id}")
        public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "Identifiant de l'utilisateur connecté", example = "42") @PathVariable Long id,
            Authentication authentication) {
        verifierAcces(id, authentication);
        return ResponseEntity.ok(userService.getProfilById(id));
    }

        /** Met à jour le profil de l'utilisateur connecté, sans modifier son rôle. */
        @Operation(summary = "UC05 - Modifier son profil", description = "Si l'email change, il doit rester unique. Les champs nom, prénom et email sont requis.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nouvelles informations du profil", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateProfilRequest.class), examples = @ExampleObject(value = "{\"nom\":\"Martin\",\"prenom\":\"Lea\",\"email\":\"lea.martin@example.com\",\"telephone\":\"+33123456789\"}"))))
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil modifié", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès à un autre profil interdit", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou téléphone déjà utilisé", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> modifierProfil(
            @Parameter(description = "Identifiant de l'utilisateur connecté", example = "42") @PathVariable Long id,
            @RequestBody UpdateProfilRequest request,
            Authentication authentication) {
        verifierAcces(id, authentication);
        return ResponseEntity.ok(userService.modifierProfil(id, request));
    }

        /** Change le mot de passe après validation de l'ancien secret. */
        @Operation(summary = "UC06 - Changer son mot de passe", description = "Le nouveau mot de passe doit avoir au moins 8 caractères et être différent de l'ancien.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Ancien et nouveau mots de passe", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChangePasswordRequest.class), examples = @ExampleObject(value = "{\"ancienMotDePasse\":\"Secret123\",\"nouveauMotDePasse\":\"AutreSecret123\"}"))))
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe modifié", content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ancien secret invalide ou nouveau mot de passe non conforme", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès à un autre compte interdit", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @PutMapping("/{id}/mot-de-passe")
    public ResponseEntity<ApiMessageResponse> changerMotDePasse(
            @Parameter(description = "Identifiant de l'utilisateur connecté", example = "42") @PathVariable Long id,
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        verifierAcces(id, authentication);
        userService.changerMotDePasse(id, request);
        return ResponseEntity.ok(new ApiMessageResponse("Le mot de passe a été modifié."));
    }

        /** Supprime le compte courant; le client devra supprimer le JWT. */
        @Operation(summary = "UC08 - Supprimer son compte", description = "Supprime le compte et ses flottes associées. Le client doit ensuite supprimer le JWT, qui reste valable jusqu'à expiration.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte supprimé"),
            @ApiResponse(responseCode = "401", description = "Authentification requise", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Suppression d'un autre compte interdite", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCompte(
            @Parameter(description = "Identifiant de l'utilisateur connecté", example = "42") @PathVariable Long id,
            Authentication authentication) {
        verifierAcces(id, authentication);
        userService.supprimerCompte(id);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Vérifie que l'appelant possède bien le profil demandé. */
    private void verifierAcces(Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserApiException(HttpStatus.UNAUTHORIZED, "Authentification requise.");
        }
        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new UserApiException(HttpStatus.UNAUTHORIZED, "Le sujet du JWT est invalide.");
        }
        if (!id.equals(userId)) {
            throw new UserApiException(HttpStatus.FORBIDDEN, "Accès interdit à ce profil.");
        }
    }
}
