import java.io.Console;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimulateurGps {
    private static final URI SERVEUR = URI.create("ws://127.0.0.1:8080/ws");
    private static final int DELAI = 10;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("java SimulateurGps.java [vehiculeId latitudeCentre longitudeCentre rayonMetres]");
            System.out.println("Par défaut : véhicule 4, centre 48.1 / 2.1, rayon 1000 m.");
            return;
        }
        try {
            simuler(args);
        } catch (Exception ex) {
            System.err.println("Simulation interrompue. " + ex.getMessage());
            System.exit(1);
        }
    }

    private static void simuler(String[] args) throws Exception {
        if (args.length != 0 && args.length != 4) {
            throw new IllegalArgumentException("Utilise --help pour voir les paramètres.");
        }
        long id = args.length == 0 ? 4 : Long.parseLong(args[0]);
        double lat = args.length == 0 ? 48.1 : Double.parseDouble(args[1]);
        double lon = args.length == 0 ? 2.1 : Double.parseDouble(args[2]);
        double rayon = args.length == 0 ? 1000 : Double.parseDouble(args[3]);
        double ecart = Math.copySign(Math.toDegrees(rayon / 6_371_000), lat >= 0 ? -1 : 1);
        if (id <= 0 || !Double.isFinite(lat) || Math.abs(lat) > 90
                || !Double.isFinite(lon) || Math.abs(lon) > 180
                || !Double.isFinite(rayon) || rayon <= 0 || Math.abs(ecart) * 2.5 > 90) {
            throw new IllegalArgumentException("Identifiant, coordonnées ou rayon invalides.");
        }

        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Lance le fichier dans un terminal PowerShell interactif.");
        }
        char[] saisie = console.readPassword("Colle l'accessToken du conducteur, puis appuie sur Entrée : ");
        if (saisie == null) {
            throw new IllegalArgumentException("Aucun jeton saisi.");
        }
        String jeton = new String(saisie).trim();
        Arrays.fill(saisie, '\0');
        if (!jeton.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Colle uniquement l'accessToken, sans Bearer ni guillemets.");
        }

        Reception reception = new Reception();
        WebSocket socket = HttpClient.newHttpClient().newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(DELAI)).subprotocols("v12.stomp")
                .buildAsync(SERVEUR, reception).get(DELAI, TimeUnit.SECONDS);
        try {
            envoyer(socket, "CONNECT\naccept-version:1.2\nhost:127.0.0.1\nheart-beat:0,0\n"
                    + "Authorization:Bearer " + jeton + "\n\n");
            String refusConnexion;
            try {
                refusConnexion = reception.connexion.get(DELAI, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                throw new IllegalStateException("Spring n'a pas répondu CONNECTED.");
            }
            if (refusConnexion != null) {
                throw new IllegalStateException(refusConnexion);
            }

            envoyer(socket, "SUBSCRIBE\nid:erreurs\ndestination:/user/queue/positions-erreurs\n\n");
            reception.attendreErreur(1);

            double[] latitudes = {lat, lat + 1.5 * ecart, lat + 2.5 * ecart, lat};
            String[] etapes = {"Au centre", "Sortie de zone", "Toujours hors zone", "Retour au centre"};
            for (int i = 0; i < latitudes.length; i++) {
                String corps = String.format(Locale.ROOT,
                        "{\"vehiculeId\":%d,\"latitude\":%s,\"longitude\":%s}", id, latitudes[i], lon);
                envoyer(socket, "SEND\ndestination:/app/positions\ncontent-type:application/json\n\n" + corps);
                System.out.printf(Locale.ROOT, "%s | véhicule %d | %.6f, %.6f%n",
                        etapes[i], id, latitudes[i], lon);
                reception.attendreErreur(3);
            }
            envoyer(socket, "DISCONNECT\n\n");
            System.out.println("Trajet envoyé. Vérifie les messages dans la console du gestionnaire.");
        } finally {
            socket.abort();
        }
    }

    private static void envoyer(WebSocket socket, String trame) throws Exception {
        socket.sendText(trame + '\0', true).get(DELAI, TimeUnit.SECONDS);
    }

    private static final class Reception implements WebSocket.Listener {
        private final CompletableFuture<String> connexion = new CompletableFuture<>();
        private final CompletableFuture<String> erreur = new CompletableFuture<>();
        private final StringBuilder tampon = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket socket, CharSequence texte, boolean last) {
            tampon.append(texte);
            int fin;
            while ((fin = tampon.indexOf("\0")) >= 0) {
                String trame = tampon.substring(0, fin).stripLeading();
                tampon.delete(0, fin + 1);
                if (trame.startsWith("CONNECTED\n")) {
                    connexion.complete(null);
                } else if (trame.startsWith("ERROR\n")) {
                    signaler("Commande STOMP refusée. Vérifie le jeton et le rôle du compte.");
                } else if (trame.startsWith("MESSAGE\n")
                        && trame.contains("subscription:erreurs\n")) {
                    int corps = trame.indexOf("\n\n");
                    signaler(corps >= 0 ? trame.substring(corps + 2) : "Position refusée.");
                }
            }
            socket.request(1);
            return null;
        }

        private void attendreErreur(int secondes) throws Exception {
            try {
                throw new IllegalStateException(erreur.get(secondes, TimeUnit.SECONDS));
            } catch (TimeoutException delaiEcoule) {
                // Aucun rejet reçu pendant cet intervalle.
            }
        }

        @Override
        public CompletionStage<?> onClose(WebSocket socket, int code, String raison) {
            signaler("Connexion WebSocket fermée (code " + code + ").");
            return null;
        }

        @Override
        public void onError(WebSocket socket, Throwable cause) {
            signaler("Connexion WebSocket interrompue.");
        }

        private void signaler(String message) {
            connexion.complete(message);
            erreur.complete(message);
        }
    }
}