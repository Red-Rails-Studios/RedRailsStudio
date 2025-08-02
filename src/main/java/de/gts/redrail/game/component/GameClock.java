package de.gts.redrail.game.component;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import static de.gts.redrail.game.constants.ResourceGeneration.RESOURCE_GENERATION_INTERVAL_IN_SECONDS;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class GameClock {

    private OffsetDateTime started;
    private OffsetDateTime ended;
    private OffsetDateTime clock;
    private OffsetDateTime nextInterval;
    private OffsetDateTime eventClock;


    public void startClock() {
        started = OffsetDateTime.now();
        ended = null; // Reset ended when starting
        clock = started;
        nextInterval = clock.plusSeconds(RESOURCE_GENERATION_INTERVAL_IN_SECONDS);
        eventClock = null;
    }
    
    public void updateClock() {
        clock = OffsetDateTime.now();
        nextInterval = clock.plusSeconds(RESOURCE_GENERATION_INTERVAL_IN_SECONDS);
    }

    public void endClock() {
        ended = OffsetDateTime.now();
        clock = null;
        nextInterval = null;
    }

    /**
     * Returns the duration in minutes between started and ended.
     * If either is null, returns 0.
     */
    public long getSessionDurationInMinutes() {
        if (started != null && ended != null) {
            return java.time.Duration.between(started, ended).toMinutes();
        }
        else if (started != null) {
            clock = OffsetDateTime.now(); // Update clock if ended is null
            return java.time.Duration.between(started, clock).toMinutes();
        }
        
        return 0;
    }

    public long getSessionDurationInSeconds() {
        if (started != null && ended != null) {
            return java.time.Duration.between(started, ended).toSeconds();
        }
        else if (started != null) {
            clock = OffsetDateTime.now(); // Update clock if ended is null
            return java.time.Duration.between(started, clock).toSeconds();
        }
        
        return 0;
    }



}
