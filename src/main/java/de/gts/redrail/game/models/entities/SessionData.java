package de.gts.redrail.game.models.entities;


import de.gts.redrail.game.component.GameClock;
import de.gts.redrail.game.constants.GameStateEnum;
import de.gts.redrail.game.models.entities.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SessionData {
    private String sessionName;
    private List<Player> sessionPlayers = new ArrayList<>();
    private GameStateEnum gameState = GameStateEnum.NOT_CREATED;
    private GameClock sessionClock = new GameClock();
}