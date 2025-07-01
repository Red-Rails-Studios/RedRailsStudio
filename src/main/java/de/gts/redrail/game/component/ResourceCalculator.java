package de.gts.redrail.game.component;

import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static de.gts.redrail.game.constants.ResourceGeneration.*;

@Component
@RequiredArgsConstructor
public class ResourceCalculator {

    private final GameClock gameClock;

    public void calculateResource(List<Player> playerList) {
        OffsetDateTime now = OffsetDateTime.now();

        if (now.isBefore(gameClock.getNextInterval())) {
            return;
        }

        Long seconds = Duration.between(gameClock.getClock(), now).getSeconds();

        for (Player player : playerList) {
            calculateResources(player, seconds);
        }

        gameClock.updateClock();
    }

    private void calculateResources(Player player, Long seconds) {
        Integer dbCoins = player.getResourceRack().getDbCoin();

        dbCoins += calculateStation(player.getStations(), seconds);
        dbCoins += calculateTrains(player.getTrains(),player.getRails(), seconds);
        player.getResourceRack().setDbCoin(dbCoins);
    }

    

    private Integer calculateTrains(List<Train> trainList,List<Rail> railList, Long seconds) {
        Integer profit = 0;

        for (Train train : trainList) {
            profit +=  (seconds.intValue() * (TRAIN_RESOURCE_GENERATION_FACTOR * train.getCapacity()));
        }
        for(Rail rail: railList){
            profit += seconds.intValue() * (rail.getLevel()*rail.getLevel());
        }

        return profit;
    }

    private Integer calculateStation(List<Station> stationList, Long seconds) {
        Integer profit = 0;

        for (Station station : stationList) {
            profit +=  seconds.intValue() * (STATION_RESOURCE_GENERATION_FACTOR*station.getLevel());
        }

        return profit;
    }
}
