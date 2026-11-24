# Simulateur GPS

Le simulateur joue le rôle d'un conducteur et envoie quatre positions par WebSocket. La démo permet de voir le véhicule sortir de sa zone autorisée, rester à l'extérieur, puis revenir au centre. Java 17 ou plus récent suffit pour lancer le simulateur.

## Démarrer le serveur

Depuis PowerShell, à la racine du dépôt, lancez MySQL et phpMyAdmin.

```powershell
docker compose up -d mysql phpmyadmin
```

Si votre environnement de développement fournit déjà `JWT_SECRET`, utilisez cette configuration. Sinon, générez une clé temporaire dans le même terminal.

```powershell
$jwtBytes = New-Object byte[] 32
$jwtGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtGenerator.GetBytes($jwtBytes)
$jwtGenerator.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

Démarrez Spring et attendez le message `Started TraqueurFlotteApplication`.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=websocket-demo" "-Dspring-boot.run.arguments=--server.address=127.0.0.1"
```

## Préparer la démo

1. Ouvrez [Swagger](http://127.0.0.1:8080/swagger-ui/index.html). Vérifiez que votre véhicule de test appartient à la flotte choisie et qu'il est affecté au conducteur. La route `GET /api/vehicules/flotte/{flotteId}` affiche les identifiants des véhicules et leurs `conducteurId`.
2. Pour la démo d'origine, utilisez la flotte `3`, le véhicule `4` et le conducteur `10`. La zone de cette flotte est centrée sur `48.1, 2.1`, avec un rayon de `1000` mètres. Ces données doivent exister dans votre base locale. Si vos identifiants diffèrent, utilisez vos propres valeurs.
3. Obtenez un `accessToken` pour le compte conducteur via `POST /api/users/connexion`. Pour observer les messages en direct, ouvrez aussi une session STOMP au nom du gestionnaire de la flotte et abonnez-la aux canaux `/topic/flottes/{flotteId}/positions` et `/topic/flottes/{flotteId}/alertes`.

### Observer les messages en direct

Dans Swagger, obtenez aussi le jeton du gestionnaire via `POST /api/users/connexion`. Sur la page Swagger, ouvrez **Inspecter → Console** dans Chrome ou Edge. Collez le script ci-dessous en adaptant `flotteId`, puis saisissez le jeton du gestionnaire dans la fenêtre qui s'ouvre.

```javascript
(() => {
    const flotteId = 3;
    const token = (prompt("Collez le jeton du gestionnaire") ?? "").trim();
    if (!token) return;
    const ws = new WebSocket(`ws://${location.host}/ws`, "v12.stomp");
    const envoyer = trame => ws.send(trame + "\0");
    ws.onopen = () => envoyer(
        `CONNECT\naccept-version:1.2\nhost:${location.hostname}\nAuthorization:Bearer ${token}\n\n`
    );
    ws.onmessage = async ({ data }) => {
        const trame = typeof data === "string" ? data : await data.text();
        if (trame.startsWith("CONNECTED")) {
            for (const canal of ["positions", "alertes"]) {
                envoyer(`SUBSCRIBE\nid:${canal}\ndestination:/topic/flottes/${flotteId}/${canal}\n\n`);
            }
            console.log("Abonnements demandés pour la flotte", flotteId);
        } else {
            console.log(trame);
        }
    };
    ws.onerror = () => console.error("Connexion WebSocket impossible.");
})();
```

## Lancer le simulateur

Dans un deuxième PowerShell, toujours à la racine du dépôt, lancez le trajet de démo.

```powershell
java .\simulateur\SimulateurGps.java
```

Collez uniquement la valeur de l'`accessToken` du conducteur quand le terminal la demande, sans guillemets ni préfixe `Bearer`. La saisie est masquée. Le simulateur contacte `ws://127.0.0.1:8080/ws`.

Pour adapter le trajet à votre base, indiquez dans l'ordre l'identifiant du véhicule, la latitude et la longitude du centre, puis le rayon en mètres.

```powershell
java .\simulateur\SimulateurGps.java 4 48.1 2.1 1000
```

## Vérifier le résultat

- Le terminal affiche quatre étapes, du départ au centre jusqu'au retour. Le gestionnaire reçoit quatre messages de position.
- Le premier point hors zone ouvre une alerte. Le deuxième point hors zone conserve cette alerte. Le retour déclenche `CLOTUREE`, avec le même `alerteId` que `OUVERTE`.
- Si une alerte était déjà ouverte, le premier point au centre la clôture. Pour identifier l'alerte de ce trajet, relevez l'identifiant de sa notification `OUVERTE`.
- Dans Swagger, autorisez séparément le jeton du gestionnaire avec **Authorize**. Consultez `GET /api/flottes/{flotteId}/positions` pour vérifier le retour au centre, puis `GET /api/flottes/{flotteId}/alertes` pour vérifier que cette alerte a un `horodatageEntree`. La même ligne est visible dans la table `alerte` de [phpMyAdmin](http://localhost:7070).
