package de.gts.redrail.game.models.entities;


import java.util.ArrayList;
import java.util.List;

import de.gts.redrail.game.component.GameClock;
import de.gts.redrail.game.constants.GameStateEnum;
import de.gts.redrail.game.models.entities.ActiveEvent;
import lombok.Data;

@Data
public class SessionData {
    private String sessionName;
    private List<Player> sessionPlayers = new ArrayList<>();
    private GameStateEnum gameState = GameStateEnum.NOT_CREATED;
    private GameClock sessionClock;
    private List<String> eventHistory = new ArrayList<>(); // Track events that happened
    private List<ActiveEvent> activeEvents = new ArrayList<>(); // Track active events
    
    public void addEventToHistory(String eventDescription) {
        eventHistory.add(eventDescription);
    }
    
    public void addActiveEvent(ActiveEvent activeEvent) {
        activeEvents.add(activeEvent);
    }
    
    public void removeActiveEvent(ActiveEvent activeEvent) {
        activeEvents.remove(activeEvent);
    }
}