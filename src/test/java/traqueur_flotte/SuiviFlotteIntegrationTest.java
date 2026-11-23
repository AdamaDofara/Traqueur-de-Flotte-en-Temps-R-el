package traqueur_flotte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import traqueur_flotte.DTO.AlerteNotificationDTO;
import traqueur_flotte.DTO.AlerteNotificationDTO.Type;
import traqueur_flotte.DTO.PositionGpsRequest;
import traqueur_flotte.DTO.UserResponseDTO;
import traqueur_flotte.model.Alerte;
import traqueur_flotte.model.Flotte;
import traqueur_flotte.model.Role;
import traqueur_flotte.model.TypeVehicule;
import traqueur_flotte.model.User;
import traqueur_flotte.model.Vehicule;
import traqueur_flotte.repository.AlerteRepository;
import traqueur_flotte.repository.FlotteRepository;
import traqueur_flotte.repository.UserRepository;
import traqueur_flotte.repository.VehiculeRepository;
import traqueur_flotte.security.JwtService;
import traqueur_flotte.services.PositionService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:suivi-flotte-test;DB_CLOSE_DELAY=-1",
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
class SuiviFlotteIntegrationTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @Autowired
    private FlotteRepository flotteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlerteRepository alerteRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean(name = "brokerMessagingTemplate", enforceOverride = true)
    private SimpMessagingTemplate messagerie;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private User gestionnaire;
    private User conducteur;
    private Flotte flotte;
    private Vehicule vehicule;

    @BeforeEach
    void preparerDonnees() {
        gestionnaire = creerCompte(Role.GESTIONNAIRE);
        conducteur = creerCompte(Role.CONDUCTEUR);

        Flotte nouvelleFlotte = new Flotte("Flotte de test", 48.1, 2.1, 1_000.0);
        nouvelleFlotte.setGestionnaire(gestionnaire);
        flotte = flotteRepository.save(nouvelleFlotte);

        Vehicule nouveauVehicule = new Vehicule(
                "TEST-" + UUID.randomUUID(), TypeVehicule.VOITURE, true
        );
        nouveauVehicule.setFlotte(flotte);
        nouveauVehicule.setConducteur(conducteur);
        vehicule = vehiculeRepository.save(nouveauVehicule);
    }

    @Test
    void trajetOuvrePuisClotureUneSeuleAlerte() {
        envoyer(48.1, 2.1);
        var positionAuCentre = vehiculeRepository.findById(vehicule.getId()).orElseThrow();

        assertEquals(48.1, positionAuCentre.getDerniereLatitude(), 0.000001);
        assertEquals(2.1, positionAuCentre.getDerniereLongitude(), 0.000001);
        assertNotNull(positionAuCentre.getDernierePositionRecueA());
        assertEquals(0, alertesDuVehicule().size());

        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            envoyer(48.12, 2.1);
            verifyNoInteractions(messagerie);
            transaction.setRollbackOnly();
        });

        assertEquals(0, alertesDuVehicule().size());
        assertEquals(48.1, vehiculeRepository.findById(vehicule.getId())
                .orElseThrow().getDerniereLatitude(), 0.000001);
        verifyNoInteractions(messagerie);

        envoyer(48.12, 2.1);
        List<Alerte> apresSortie = alertesDuVehicule();
        assertEquals(1, apresSortie.size());

        Alerte alerteOuverte = apresSortie.get(0);
        assertNotNull(alerteOuverte.getHorodatageSortie());
        assertNull(alerteOuverte.getHorodatageEntree());
        assertEquals(48.12, alerteOuverte.getLatitudeSortie(), 0.000001);

        envoyer(48.13, 2.1);
        List<Alerte> encoreDehors = alertesDuVehicule();
        assertEquals(1, encoreDehors.size());
        assertEquals(alerteOuverte.getId(), encoreDehors.get(0).getId());
        assertNull(encoreDehors.get(0).getHorodatageEntree());
        assertEquals(48.13, vehiculeRepository.findById(vehicule.getId())
                .orElseThrow().getDerniereLatitude(), 0.000001);

        envoyer(48.101, 2.102);
        List<Alerte> apresRetour = alertesDuVehicule();
        assertEquals(1, apresRetour.size());

        Alerte alerteCloturee = apresRetour.get(0);
        assertEquals(alerteOuverte.getId(), alerteCloturee.getId());
        assertNotNull(alerteCloturee.getHorodatageEntree());
        assertEquals(48.101, alerteCloturee.getLatitudeEntree(), 0.000001);
        assertEquals(2.102, alerteCloturee.getLongitudeEntree(), 0.000001);

        Vehicule enBase = vehiculeRepository.findById(vehicule.getId()).orElseThrow();
        assertEquals(48.101, enBase.getDerniereLatitude(), 0.000001);
        assertEquals(2.102, enBase.getDerniereLongitude(), 0.000001);

        String destination = "/topic/flottes/" + flotte.getId() + "/alertes";
        var notifications = ArgumentCaptor.forClass(AlerteNotificationDTO.class);
        verify(messagerie, times(2)).convertAndSend(
                eq(destination), notifications.capture()
        );

        List<AlerteNotificationDTO> messages = notifications.getAllValues();
        assertEquals(Type.OUVERTE, messages.get(0).type());
        assertEquals(Type.CLOTUREE, messages.get(1).type());
        assertEquals(alerteOuverte.getId(), messages.get(0).alerteId());
        assertEquals(alerteOuverte.getId(), messages.get(1).alerteId());
    }

    @Test
    void routesRestVerifientLeJwtEtLesDroitsSurLaFlotte() throws Exception {
        User autreGestionnaire = creerCompte(Role.GESTIONNAIRE);
        String jetonAutorise = creerJeton(gestionnaire);
        String jetonInterdit = creerJeton(autreGestionnaire);

        envoyer(48.12, 2.1);

        for (String ressource : List.of("positions", "alertes")) {
            assertEquals(200, statutHttp(ressource, jetonAutorise), ressource);
            assertEquals(403, statutHttp(ressource, jetonInterdit), ressource);
            assertEquals(401, statutHttp(ressource, null), ressource);
        }
    }

    private void envoyer(double latitude, double longitude) {
        positionService.preparerPosition(
                new PositionGpsRequest(vehicule.getId(), latitude, longitude),
                conducteur.getId()
        );
    }

    private List<Alerte> alertesDuVehicule() {
        return alerteRepository.findByVehiculeId(vehicule.getId());
    }

    private User creerCompte(Role role) {
        User utilisateur = new User();
        utilisateur.setNom("Test");
        utilisateur.setPrenom("Suivi");
        utilisateur.setEmail(UUID.randomUUID() + "@example.test");
        utilisateur.setMotDePasse("Fixture-test");
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        return userRepository.save(utilisateur);
    }

    private String creerJeton(User utilisateur) {
        var profil = new UserResponseDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getRole(),
                utilisateur.isActif()
        );

        return jwtService.generateToken(profil).value();
    }

    private int statutHttp(String ressource, String jeton) throws Exception {
        URI adresse = URI.create(
                "http://127.0.0.1:" + port + "/api/flottes/" + flotte.getId()
                        + "/" + ressource
        );

        HttpRequest.Builder requete = HttpRequest.newBuilder(adresse)
                .timeout(Duration.ofSeconds(10))
                .GET();

        if (jeton != null) {
            requete.header("Authorization", "Bearer " + jeton);
        }

        return httpClient.send(requete.build(), HttpResponse.BodyHandlers.discarding())
                .statusCode();
    }
}