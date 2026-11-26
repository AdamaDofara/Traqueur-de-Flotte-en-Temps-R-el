package traqueur_flotte.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import traqueur_flotte.DTO.AlerteNotificationDTO;
import traqueur_flotte.DTO.AlerteNotificationDTO.Type;
import traqueur_flotte.services.ZoneFlotteService.NotificationAlerte;

@Component
@Profile("websocket-demo")
public class AlerteWebSocketPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AlerteWebSocketPublisher.class);

    private final SimpMessagingTemplate messagerie;

    public AlerteWebSocketPublisher(SimpMessagingTemplate messagerie) {
        this.messagerie = messagerie;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void diffuser(NotificationAlerte evenement) {
        AlerteNotificationDTO notification = evenement.message();
        if (notification.type() != Type.RAPPEL) {
            envoyer("/topic/flottes/" + notification.flotteId() + "/alertes", notification);
        }
        if (evenement.conducteurId() != null) {
            envoyer("/topic/conducteur/" + evenement.conducteurId() + "/alertes", notification);
        }
    }

    private void envoyer(String destination, AlerteNotificationDTO notification) {
        try {
            messagerie.convertAndSend(destination, notification);
        } catch (MessagingException exception) {
            LOGGER.warn(
                    "Impossible de diffuser la notification {} de l'alerte {}.",
                    notification.type(), notification.alerteId(), exception
            );
        }
    }
}