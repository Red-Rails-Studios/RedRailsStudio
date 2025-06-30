package de.gts.redrail.game.models.dtos;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEndResponseDto {
    private long duration;
   
    private List<PlayerOverviewDto> players;


    public SessionEndResponseDto(List<PlayerOverviewDto> players, long duration) {
            this.players = players;
            this.duration = duration;
    }

    
}