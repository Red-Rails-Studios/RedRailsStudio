package de.gts.redrail.game.models.entities;

import de.gts.redrail.game.models.entities.GameEvent;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class ActiveEvent {
    private GameEvent event;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private boolean isActive;
    
    public ActiveEvent(GameEvent event) {
        this.event = event;
        this.startTime = OffsetDateTime.now();
        this.endTime = startTime.plusMinutes(1); // Event lasts 1 minute
        this.isActive = true;
    }
    
    public boolean hasExpired() {
        return OffsetDateTime.now().isAfter(endTime);
    }
}