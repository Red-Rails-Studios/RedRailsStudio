package de.gts.redrail.game.utils;

import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import de.gts.redrail.game.models.entities.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class PlayerUtil {

    public static boolean isPlayerMatching(Player player, PlayerOverviewDto playerOverviewDto) {
        if (player == null || playerOverviewDto == null) {
            return false;
        }

        if (StringUtils.isEmpty(player.getUId()) || StringUtils.isEmpty(playerOverviewDto.getUid())) {
            return false;
        }

        return player.getUId().equals(playerOverviewDto.getUid());
    }

    public static Optional<Player> getPlayerByUid(List<Player> playerList, String uId) {
        if (StringUtils.isBlank(uId)) {
            return Optional.empty();
        }

        for (Player player : playerList) {
            if (player.getUId().equals(uId)) {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }
}
