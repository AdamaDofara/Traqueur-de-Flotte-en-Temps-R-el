package traqueur_flotte.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import traqueur_flotte.DTO.PositionGpsMessage;
import traqueur_flotte.DTO.PositionGpsRequest;
import traqueur_flotte.services.PositionService;

import java.security.Principal;

@Controller
@Profile("websocket-demo")
public class PositionWebSocketController {

    private final PositionService positionService;
    private final SimpMessagingTemplate messagerie;

    public PositionWebSocketController(
            PositionService positionService,
            SimpMessagingTemplate messagerie
    ) {
        this.positionService = positionService;
        this.messagerie = messagerie;
    }

    @MessageMapping("/positions")
    public void recevoirPosition(PositionGpsRequest demande, Principal utilisateur) {
        Long conducteurId = Long.valueOf(utilisateur.getName());
        PositionGpsMessage position = positionService.preparerPosition(demande, conducteurId);

        String destination = "/topic/flottes/" + position.flotteId() + "/positions";
        messagerie.convertAndSend(destination, position);
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser(value = "/queue/positions-erreurs", broadcast = false)
    public String positionInvalide(IllegalArgumentException erreur) {
        return erreur.getMessage();
    }
}