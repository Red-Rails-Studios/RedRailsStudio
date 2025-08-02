package de.gts.redrail.game.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.ActiveEvent;
import de.gts.redrail.game.models.entities.GameEvent;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.SessionData;

@Service
public class EventService {
    
    public ActionResult triggerRandomEvent(SessionData sessionData) {
        // Check for expired events first
        checkAndReverseExpiredEvents(sessionData);
        
        GameEvent randomEvent = GameEvent.getRandomEvent();
        
        // Apply event to all players
        for (Player player : sessionData.getSessionPlayers()) {
            randomEvent.apply(player);
        }
        
        // Track the active event
        ActiveEvent activeEvent = new ActiveEvent(randomEvent);
        sessionData.addActiveEvent(activeEvent);
        
        // Add to history
        sessionData.addEventToHistory(randomEvent.getName() + ": " + randomEvent.getDescription());
        
        return new ActionResult(true, 
            "Event triggered: " + randomEvent.getName() + " - " + randomEvent.getDescription() + " (Duration: 1 minute)",
            randomEvent.name());
    }
    
    public void checkAndReverseExpiredEvents(SessionData sessionData) {
        Iterator<ActiveEvent> iterator = sessionData.getActiveEvents().iterator();
        
        while (iterator.hasNext()) {
            ActiveEvent activeEvent = iterator.next();
            
            if (activeEvent.hasExpired()) {
                // Reverse the event effects for all players
                for (Player player : sessionData.getSessionPlayers()) {
                    activeEvent.getEvent().reverse(player);
                }
                
                // Mark as inactive and remove from active events
                activeEvent.setActive(false);
                iterator.remove();
                
                // Add reversal to history
                sessionData.addEventToHistory(activeEvent.getEvent().getName() + " has ended - effects reversed");
            }
        }
    }
    
    public List<ActiveEvent> getActiveEvents(SessionData sessionData) {
        return new ArrayList<>(sessionData.getActiveEvents());
    }
}
