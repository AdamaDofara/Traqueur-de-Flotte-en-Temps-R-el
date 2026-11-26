package traqueur_flotte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import tools.jackson.databind.json.JsonMapper;
import traqueur_flotte.DTO.AlerteNotificationDTO;
import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.DTO.PositionGpsRequest;
import traqueur_flotte.DTO.UserResponseDTO;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.Role;
import traqueur_flotte.model.TypeVehicule;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;
import traqueur_flotte.repository.VehiculeRepository;
import traqueur_flotte.security.JwtService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.address=127.0.0.1",
                "spring.datasource.url=jdbc:h2:mem:suivi-websocket-test;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.show-sql=false",
                "JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
        }
)
@ActiveProfiles("websocket-demo")
class SuiviWebSocketIntegrationTest {

    private static final int DELAI_SECONDES = 10;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlotteRepository flotteRepository;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SimpleBrokerMessageHandler broker;

    private final JsonMapper json = JsonMapper.builder().build();
    private final List<StompSession> sessions = new ArrayList<>();

    private WebSocketStompClient client;
    private User gestionnaire;
    private User conducteur;
    private Flotte flotte;
    private Vehicule vehicule;

    @BeforeEach
    void preparerTest() {
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new SimpleMessageConverter());
        client.setDefaultHeartbeat(new long[] {0, 0});

        gestionnaire = creerCompte(Role.GESTIONNAIRE);
        conducteur = creerCompte(Role.CONDUCTEUR);
        flotte = creerFlotte(gestionnaire);

        Vehicule nouveauVehicule = new Vehicule(
                "WS-" + UUID.randomUUID(), TypeVehicule.VOITURE, true
        );
        nouveauVehicule.setFlotte(flotte);
        nouveauVehicule.setConducteur(conducteur);
        vehicule = vehiculeRepository.save(nouveauVehicule);
    }

    @AfterEach
    void fermerConnexions() {
        try {
            for (StompSession session : sessions) {
                if (session.isConnected()) {
                    session.disconnect();
                }
            }
        } finally {
            if (client != null) {
                client.stop();
            }
        }
    }

    @Test
    void gestionnaireRecoitLesPositionsEtLesDeuxNotifications() throws Exception {
        StompSession suivi = connecter(gestionnaire, new StompSessionHandlerAdapter() {});
        StompSession emission = connecter(conducteur, new StompSessionHandlerAdapter() {});

        BlockingQueue<byte[]> positions = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> alertes = new LinkedBlockingQueue<>();

        String canalPositions = canal("positions");
        String canalAlertes = canal("alertes");

        suivi.subscribe(canalPositions, recevoirDans(positions));
        suivi.subscribe(canalAlertes, recevoirDans(alertes));
        attendreAbonnement(canalPositions);
        attendreAbonnement(canalAlertes);

        BlockingQueue<byte[]> alertesConducteur = new LinkedBlockingQueue<>();
        String canalConducteur = "/topic/conducteur/" + conducteur.getId() + "/alertes";
        emission.subscribe(canalConducteur, recevoirDans(alertesConducteur));
        attendreAbonnement(canalConducteur);

        envoyerPosition(emission, 48.12, 2.1);
        verifierPosition(positions, 48.12, 2.1);
        AlerteNotificationDTO ouverture = lireMessage(alertes, AlerteNotificationDTO.class);

        assertEquals(AlerteNotificationDTO.Type.OUVERTE, ouverture.type());
        assertNotNull(ouverture.alerteId());
        assertEquals(flotte.getId(), ouverture.flotteId());
        assertEquals(vehicule.getId(), ouverture.vehiculeId());
        assertEquals(48.12, ouverture.latitude(), 0.000001);
        assertEquals(vehicule.getImmatriculation(), ouverture.immatriculation());
        assertEquals(2.1, ouverture.longitude(), 0.000001);
        assertNotNull(ouverture.horodatage());
        assertEquals(ouverture, lireMessage(alertesConducteur, AlerteNotificationDTO.class));

        envoyerPosition(emission, 48.13, 2.1);
        verifierPosition(positions, 48.13, 2.1);
        var rappel = lireMessage(alertesConducteur, AlerteNotificationDTO.class);
        assertEquals(AlerteNotificationDTO.Type.RAPPEL, rappel.type());
        assertEquals(ouverture.alerteId(), rappel.alerteId());
        assertEquals(48.13, rappel.latitude(), 0.000001);

        envoyerPosition(emission, 48.1, 2.1);
        verifierPosition(positions, 48.1, 2.1);
        AlerteNotificationDTO cloture = lireMessage(alertes, AlerteNotificationDTO.class);

        assertEquals(AlerteNotificationDTO.Type.CLOTUREE, cloture.type());
        assertEquals(ouverture.alerteId(), cloture.alerteId());
        assertEquals(flotte.getId(), cloture.flotteId());
        assertEquals(vehicule.getId(), cloture.vehiculeId());
        assertEquals(48.1, cloture.latitude(), 0.000001);
        assertEquals(cloture, lireMessage(alertesConducteur, AlerteNotificationDTO.class));

        assertNull(alertes.poll(500, TimeUnit.MILLISECONDS),
                "Le trajet doit produire uniquement une ouverture et une clôture.");
        assertTrue(positions.isEmpty(), "Trois positions doivent être reçues.");
    }

    @Test
    void gestionnaireDuneAutreFlotteNePeutPasSabonnerAuxAlertes() throws Exception {
        User autreGestionnaire = creerCompte(Role.GESTIONNAIRE);
        creerFlotte(autreGestionnaire);

        verifierAbonnementRefuse(autreGestionnaire, canal("alertes"));
    }

    @Test
    void conducteurNePeutPasSabonnerAuxAlertesDunAutreConducteur() throws Exception {
        User autreConducteur = creerCompte(Role.CONDUCTEUR);
        verifierAbonnementRefuse(autreConducteur,
                "/topic/conducteur/" + conducteur.getId() + "/alertes");
    }

    private void verifierAbonnementRefuse(User compte, String destination) throws Exception {
        CompletableFuture<StompHeaders> erreurStomp = new CompletableFuture<>();

        StompSession suiviInterdit = connecter(
                compte,
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        erreurStomp.complete(headers);
                    }
                }
        );

        BlockingQueue<byte[]> messages = new LinkedBlockingQueue<>();
        suiviInterdit.subscribe(destination, recevoirDans(messages));

        StompHeaders erreur = erreurStomp.get(DELAI_SECONDES, TimeUnit.SECONDS);
        assertNotNull(erreur.getFirst("message"), "Le serveur doit renvoyer une trame ERROR.");
        assertTrue(messages.isEmpty());
        assertTrue(abonnements(destination).isEmpty(),
                "Le broker ne doit pas enregistrer l'abonnement interdit.");
    }

    private StompSession connecter(User utilisateur, StompSessionHandlerAdapter handler)
            throws Exception {
        var profil = new UserResponseDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getRole(),
                utilisateur.isActif()
        );

        String jeton = jwtService.generateToken(profil).value();
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + jeton);

        StompSession session = client.connectAsync(
                "ws://127.0.0.1:" + port + "/ws", new WebSocketHttpHeaders(), headers, handler
        ).get(DELAI_SECONDES, TimeUnit.SECONDS);

        sessions.add(session);
        return session;
    }

    private User creerCompte(Role role) {
        User utilisateur = new User();
        utilisateur.setNom("Test");
        utilisateur.setPrenom("WebSocket");
        utilisateur.setEmail(UUID.randomUUID() + "@example.test");
        utilisateur.setMotDePasse("Fixture-test");
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        return userRepository.save(utilisateur);
    }

    private Flotte creerFlotte(User proprietaire) {
        Flotte nouvelleFlotte = new Flotte("Flotte WebSocket", 48.1, 2.1, 1_000.0);
        nouvelleFlotte.setGestionnaire(proprietaire);
        return flotteRepository.save(nouvelleFlotte);
    }

    private String canal(String ressource) {
        return "/topic/flottes/" + flotte.getId() + "/" + ressource;
    }

    private StompFrameHandler recevoirDans(BlockingQueue<byte[]> messages) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.add((byte[]) payload);
            }
        };
    }

    private void attendreAbonnement(String destination) throws InterruptedException {
        long limite = System.nanoTime() + Duration.ofSeconds(DELAI_SECONDES).toNanos();

        while (abonnements(destination).isEmpty() && System.nanoTime() < limite) {
            Thread.sleep(20);
        }

        assertTrue(!abonnements(destination).isEmpty(),
                "Abonnement non enregistré par le broker sur " + destination);
    }

    private org.springframework.util.MultiValueMap<String, String> abonnements(
            String destination
    ) {
        var headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setDestination(destination);
        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0], headers.getMessageHeaders()
        );
        return broker.getSubscriptionRegistry().findSubscriptions(message);
    }

    private void envoyerPosition(StompSession session, double latitude, double longitude) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/app/positions");
        headers.setContentType(MimeTypeUtils.APPLICATION_JSON);

        byte[] corps = json.writeValueAsBytes(
                new PositionGpsRequest(vehicule.getId(), latitude, longitude)
        );
        session.send(headers, corps);
    }

    private void verifierPosition(
            BlockingQueue<byte[]> positions, double latitude, double longitude
    ) throws InterruptedException {
        PositionGpsMessage position = lireMessage(positions, PositionGpsMessage.class);

        assertEquals(vehicule.getId(), position.vehiculeId());
        assertEquals(flotte.getId(), position.flotteId());
        assertEquals(latitude, position.latitude(), 0.000001);
        assertEquals(longitude, position.longitude(), 0.000001);
        assertNotNull(position.recueA());
    }

    private <T> T lireMessage(BlockingQueue<byte[]> messages, Class<T> type)
            throws InterruptedException {
        byte[] corps = messages.poll(DELAI_SECONDES, TimeUnit.SECONDS);
        assertNotNull(corps, "Aucun message reçu pour " + type.getSimpleName());
        return json.readValue(corps, type);
    }
}