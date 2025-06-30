package de.gts.redrail.game.models.dtos;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import de.gts.redrail.game.component.GameClock;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEndResponseDto {
    private GameClock gameClock;
    private long duration = gameClock.getSessionDurationInMinutes(); // in minutes
   
    private List<PlayerOverviewDto> players;


    public SessionEndResponseDto(List<PlayerOverviewDto> players) {
            this.players = players;
    }

    
}