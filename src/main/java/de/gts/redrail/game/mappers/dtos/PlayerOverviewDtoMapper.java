package de.gts.redrail.game.mappers.dtos;

import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import de.gts.redrail.game.models.entities.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerOverviewDtoMapper {

    public PlayerOverviewDto map(Player player) {
        if (player == null) {
            return null;
        }

        PlayerOverviewDto playerOverviewDto = new PlayerOverviewDto();

        playerOverviewDto.setName(player.getName());
        playerOverviewDto.setUId(player.getUId());
        playerOverviewDto.setColor(player.getColor());

        return playerOverviewDto;
    }

    public List<PlayerOverviewDto> map(List<Player> playerList) {
       if (playerList == null) {
           return null;
       }

       List<PlayerOverviewDto> playerOverviewDtoList = new ArrayList<>();

       for (Player player : playerList) {
           PlayerOverviewDto playerOverviewDto = map(player);

           if (playerOverviewDto != null) {
               playerOverviewDtoList.add(playerOverviewDto);
           }
       }

       return playerOverviewDtoList;
    }
}
