package de.gts.redrail.game.component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import static de.gts.redrail.game.constants.ResourceGeneration.STATION_RESOURCE_GENERATION_FACTOR;
import static de.gts.redrail.game.constants.ResourceGeneration.TRAIN_RESOURCE_GENERATION_FACTOR;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;

@Component
public class ResourceCalculator {

    public void calculateResource(List<Player> playerList, GameClock sessionClock) {
        if (sessionClock == null || sessionClock.getNextInterval() == null || sessionClock.getClock() == null) {
            return; // Skip calculation if clock is not properly initialized
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(sessionClock.getNextInterval())) {
            return;
        }

        Long seconds = Duration.between(sessionClock.getClock(), now).getSeconds();

        for (Player player : playerList) {
            calculateResources(player, seconds);
        }

        sessionClock.updateClock();
    }

    private void calculateResources(Player player, Long seconds) {
        Integer dbCoins = player.getResourceRack().getDbCoin();

        dbCoins += calculateStation(player.getStations(), seconds);
        dbCoins += calculateTrains(player.getTrains(), player.getRails(), seconds);
        player.getResourceRack().setDbCoin(dbCoins);
    }

    private Integer calculateTrains(List<Train> trainList, List<Rail> railList, Long seconds) {
        Integer profit = 0;

        for (Train train : trainList) {
            profit += (seconds.intValue() * (TRAIN_RESOURCE_GENERATION_FACTOR * train.getCapacity()));
        }

        for (Rail rail : railList) {
            profit += seconds.intValue() * (rail.getLevel() * rail.getLevel());
        }

        return profit;
    }

    private Integer calculateStation(List<Station> stationList, Long seconds) {
        Integer profit = 0;

        for (Station station : stationList) {
            profit += seconds.intValue() * (STATION_RESOURCE_GENERATION_FACTOR * station.getLevel());
        }

        return profit;
    }
}
