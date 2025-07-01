package de.gts.redrail.game.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

import static de.gts.redrail.game.constants.ResourceGeneration.RESOURCE_GENERATION_INTERVAL_IN_SECONDS;

@Component
@Getter
@Setter
public class GameClock {

    private OffsetDateTime started;
    private OffsetDateTime ended;
    private OffsetDateTime clock;
    private OffsetDateTime nextInterval;


    public void startClock() {
        started = OffsetDateTime.now();
        clock = started;
        nextInterval = clock.plusSeconds(RESOURCE_GENERATION_INTERVAL_IN_SECONDS);
    }
    public void updateClock() {
        clock = OffsetDateTime.now();
        nextInterval = clock.plusSeconds(RESOURCE_GENERATION_INTERVAL_IN_SECONDS);
    }

    public void endClock() {
        clock = null;
        ended = OffsetDateTime.now();
    }

    /**
     * Returns the duration in minutes between started and ended.
     * If either is null, returns 0.
     */
    public long getSessionDurationInMinutes() {
        if (started != null && ended != null) {
            return java.time.Duration.between(started, ended).toMinutes();
        }
        
        return 0;
    }

}
