# Authentification et gestion des utilisateurs

Cette documentation couvre les cas UC01 à UC08 implémentés par `UserController` et `UserService`. Les routes sont disponibles sous `/api/users`. L’authentification utilise des access tokens JWT HS256 vérifiés par Spring Security OAuth2 Resource Server. Aucun état de session n’est conservé côté serveur.

## Swagger / OpenAPI

Démarrer l’application, puis ouvrir [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). Le document OpenAPI est accessible sur `/v3/api-docs`. Swagger UI et les documents OpenAPI sont autorisés dans `SecurityConfig`.

Les opérations d’authentification et de profil sont annotées avec leurs paramètres, exemples, schémas de réponse et codes d’erreur. La dépendance `springdoc-openapi-starter-webmvc-ui` est déjà présente dans `pom.xml`.

## Fonctionnement JWT

- À l’inscription, le mot de passe est encodé avec BCrypt avant sauvegarde. Les réponses ne contiennent ni le hash ni le token de réinitialisation.
- À la connexion, les identifiants sont vérifiés et un JWT signé en HS256 est retourné. Le token contient `iss`, `sub` (identifiant utilisateur), `iat`, `exp` et `roles`.
- La durée par défaut est de 15 minutes; elle peut être modifiée par `JWT_ACCESS_TOKEN_MINUTES`.
- Pour les appels suivants, transmettre `Authorization: Bearer <accessToken>`.
- Spring Security vérifie la signature, les dates de validité et l’émetteur `traqueur-flotte`. Les rôles deviennent des autorités `ROLE_GESTIONNAIRE`, `ROLE_CONDUCTEUR` ou `ROLE_ADMINISTRATEUR`.
- La déconnexion est stateless: elle ne révoque pas un token déjà émis. Le client doit supprimer son access token; le token reste utilisable au plus jusqu’à son expiration. Un mécanisme de révocation ou des refresh tokens nécessiterait un stockage serveur supplémentaire et n’est pas inclus.
- La configuration globale conserve `permitAll()` pour ne pas bloquer les routes métier existantes. Seuls les contrôles ajoutés par `UserController` protègent actuellement les opérations de profil et de suppression. Les contrôleurs des autres modules doivent vérifier explicitement l’identité et les autorisations nécessaires; la présence d’un JWT ne protège pas automatiquement leurs routes.

### Clé de signature

La clé n’est pas stockée dans le dépôt. Définir `JWT_SECRET` dans l’environnement avant de démarrer l’application. Elle doit être une chaîne Base64 décodant vers au moins 32 octets aléatoires. Exemple PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:JWT_ACCESS_TOKEN_MINUTES = "15"
```

Tous les serveurs d’une même application doivent partager la même clé. En production, la fournir depuis un gestionnaire de secrets et la faire tourner selon une procédure planifiée.

## Endpoints

Les exemples utilisent `http://localhost:8080` comme origine. Les réponses d’erreur métier suivent le format `{"erreur":"..."}`. Une requête JSON mal formée peut également être rejetée par Spring avant le traitement métier.

### UC01 - Inscription

`POST /api/users/inscription`

```json
{
  "nom": "Martin",
  "prenom": "Lea",
  "email": "lea.martin@example.com",
  "motDePasse": "Secret123",
  "role": "GESTIONNAIRE",
  "telephone": "+33123456789"
}
```

`telephone` est facultatif. `nom`, `prenom`, `email`, `motDePasse` et `role` sont requis; le mot de passe doit avoir au moins 8 caractères. Le compte est créé actif.

Réponse `201 Created`:

```json
{
  "id": 42,
  "nom": "Martin",
  "prenom": "Lea",
  "email": "lea.martin@example.com",
  "telephone": "+33123456789",
  "role": "GESTIONNAIRE",
  "actif": true
}
```

Erreurs: `400 Bad Request` si un champ est invalide; `409 Conflict` si l’email ou le téléphone est déjà utilisé.

### UC02 - Connexion

`POST /api/users/connexion`

```json
{
  "email": "lea.martin@example.com",
  "motDePasse": "Secret123"
}
```

Réponse `200 OK`:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "utilisateur": {
    "id": 42,
    "nom": "Martin",
    "prenom": "Lea",
    "email": "lea.martin@example.com",
    "telephone": "+33123456789",
    "role": "GESTIONNAIRE",
    "actif": true
  },
  "message": "Connexion réussie."
}
```

Erreurs: `401 Unauthorized` si les identifiants ne correspondent pas; `403 Forbidden` si le compte est inactif.

### UC03 - Déconnexion

`POST /api/users/deconnexion`

Réponse `200 OK`, sans corps. Le client supprime son token local. Le token présenté n’est pas révoqué côté serveur et reste valide jusqu’à son expiration.

### UC04 - Consulter son profil

`GET /api/users/{id}`

Exemple: `GET /api/users/42` avec `Authorization: Bearer <accessToken>`.

Réponse `200 OK`: même schéma de profil public que celui présenté à l’inscription. Erreurs: `401 Unauthorized` si aucune session n’est établie; `403 Forbidden` si l’identifiant demandé n’est pas celui de l’utilisateur connecté; `404 Not Found` si le compte n’existe pas.

### UC05 - Modifier son profil

`PUT /api/users/{id}` avec `Authorization: Bearer <accessToken>`:

```json
{
  "nom": "Martin",
  "prenom": "Lea",
  "email": "lea.martin@example.com",
  "telephone": "+33123456789"
}
```

`nom`, `prenom` et `email` sont requis. `telephone` est facultatif; s’il est absent, la valeur actuelle est conservée. Réponse `200 OK` avec le profil à jour. Erreurs: `400`, `401`, `403`, `404` ou `409` si une adresse email ou un téléphone unique est déjà utilisé.

### UC06 - Changer son mot de passe

`PUT /api/users/{id}/mot-de-passe` avec `Authorization: Bearer <accessToken>`:

```json
{
  "ancienMotDePasse": "Secret123",
  "nouveauMotDePasse": "AutreSecret123"
}
```

Réponse `200 OK`:

```json
{"message":"Le mot de passe a été modifié."}
```

Erreurs: `400 Bad Request` si l’ancien mot de passe est incorrect, si le nouveau fait moins de 8 caractères ou s’il est identique à l’ancien; `401`, `403` ou `404` selon l’accès et l’existence du compte.

### UC07 - Mot de passe oublié et réinitialisation

Demander un token avec `POST /api/users/mot-de-passe-oublie`:

```json
{"email":"lea.martin@example.com"}
```

Pour un compte connu, réponse `200 OK`:

```json
{"message":"Un token a été généré et journalisé."}
```

Le token UUID a une durée de validité de 30 minutes. Il est actuellement écrit dans les logs applicatifs (aucun service d’envoi de courriel n’est configuré). Un email invalide donne `400`; un compte inconnu donne `404`.

Terminer la réinitialisation avec `POST /api/users/reinitialiser-mot-de-passe`:

```json
{
  "token": "uuid-copie-depuis-les-logs",
  "nouveauMotDePasse": "NouveauSecret123"
}
```

Réponse `200 OK`:

```json
{"message":"Le mot de passe a été réinitialisé."}
```

Un token absent, inconnu ou expiré, ou un nouveau mot de passe trop court, donne `400 Bad Request`. Le token est consommé après succès.

### UC08 - Supprimer son compte

`DELETE /api/users/{id}` avec `Authorization: Bearer <accessToken>`. Réponse `204 No Content`; le compte et ses flottes associées sont supprimés. Le client doit ensuite supprimer le token, qui reste autrement valide jusqu’à expiration. Erreurs: `401`, `403` ou `404`.

La suppression en cascade est définie sur la relation `User.flottes`. La suppression peut toutefois échouer si d’autres relations en base, par exemple un véhicule conducteur associé, imposent une contrainte référentielle; cette dépendance est à valider avec les responsables des entités métier concernées.

## Tester avec curl

Sur PowerShell, le wrapper `curl` peut être un alias de `Invoke-WebRequest`; utiliser `curl.exe` pour les exemples ci-dessous.

```powershell
$login = curl.exe -s -H "Content-Type: application/json" -d '{"email":"lea.martin@example.com","motDePasse":"Secret123"}' http://localhost:8080/api/users/connexion | ConvertFrom-Json
$token = $login.accessToken
curl.exe -i -H "Authorization: Bearer $token" http://localhost:8080/api/users/42
curl.exe -i -X POST http://localhost:8080/api/users/deconnexion
```

Dans Swagger UI, exécuter d’abord l’opération de connexion puis copier `accessToken`. Cliquer sur **Authorize** et saisir `Bearer <accessToken>` (ou le token si Swagger ajoute automatiquement le préfixe Bearer); les opérations de profil et suppression déclarent le schéma `bearerAuth`. Ne pas copier un mot de passe ou la clé de signature dans un dépôt ou des logs.

## Intégration par les autres contrôleurs

Le client transmet `Authorization: Bearer <accessToken>`.

Pour récupérer l’identité dans un contrôleur Spring MVC, injecter `Authentication` comme dans `UserController`, puis lire `authentication.getName()` (le `sub`, converti en identifiant utilisateur) et les autorités avec `authentication.getAuthorities()`. Les contrôleurs métier doivent vérifier l’authentification et les rôles appropriés avant d’effectuer une opération. À ce stade, `SecurityConfig` laisse volontairement toutes les routes en `permitAll()` pour préserver les routes existantes; les modules métier ne sont donc pas protégés automatiquement. Un Bearer fourni est néanmoins vérifié par le filtre Resource Server et son contexte est disponible.

Transmettre les tokens uniquement sur HTTPS en production et les conserver côté client avec soin. L’API désactive CSRF et n’émet pas de refresh token. Les JWT restent valides jusqu’à expiration, notamment après une déconnexion ou un changement de mot de passe.

## Notes de schéma et de migration

Hibernate est configuré avec `ddl-auto=update`; au démarrage, il doit ajouter les champs de token de réinitialisation et rendre `telephone` nullable. Les anciens mots de passe stockés en clair ne correspondent pas aux nouveaux hashes BCrypt et nécessitent une migration dédiée ou une réinitialisation par UC07.


