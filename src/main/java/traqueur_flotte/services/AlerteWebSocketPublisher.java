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
    public void diffuser(AlerteNotificationDTO notification) {
        String destination = "/topic/flottes/" + notification.flotteId() + "/alertes";

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