package de.gts.redrail.game.models.dtos;

import java.time.OffsetDateTime;
import java.util.List;

import de.gts.redrail.game.constants.GameStateEnum;
import de.gts.redrail.game.models.entities.Player;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SessionOverviewDto {
    String sessionName;
    List<Player> players;
    GameStateEnum gameState;
    OffsetDateTime sessionStarted;
    OffsetDateTime sessionEnded;
}
