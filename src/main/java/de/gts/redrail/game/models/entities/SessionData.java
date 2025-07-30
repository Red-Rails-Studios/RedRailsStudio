package de.gts.redrail.game.models.entities;


import java.util.List;

import de.gts.redrail.game.component.GameClock;
import de.gts.redrail.game.constants.GameStateEnum;
import lombok.Data;

@Data
public class SessionData {
    private String sessionName;
    private List<Player> sessionPlayers;
    private GameStateEnum gameState = GameStateEnum.NOT_CREATED;
    private GameClock sessionClock;
    
}