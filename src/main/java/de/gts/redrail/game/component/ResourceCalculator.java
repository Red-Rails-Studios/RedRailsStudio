package de.gts.redrail.game.component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import static de.gts.redrail.game.constants.ResourceGeneration.RESOURCE_GENERATION_INTERVAL_IN_SECONDS;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.service.MapSpaceGenerationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResourceCalculator {

    private final MapSpaceGenerationService mapService;

    public void calculateResource(List<Player> playerList, GameClock sessionClock) {
        if (sessionClock == null || sessionClock.getNextInterval() == null || sessionClock.getClock() == null) {
            return; 
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

        dbCoins += calculateTrainIncomeByStation(player, seconds);

    
        dbCoins += calculateRails(player.getRails(), seconds);

        player.getResourceRack().setDbCoin(dbCoins);
    }

    private Integer calculateTrainIncomeByStation(Player player, Long seconds) {
        int profit = 0;

        if (player.getStations() == null || player.getStations().isEmpty()) {
            if (player.getTrains() != null) {
                int ticks = Math.max(1, (int) (seconds / RESOURCE_GENERATION_INTERVAL_IN_SECONDS));
                for (Train t : player.getTrains()) {
                    int cap = t.getCapacity() == null ? 0 : t.getCapacity().intValue();
                    int fibIndex = Math.min(12, Math.max(0, cap));
                    int weight = fib(fibIndex + 1);
                    profit += ticks * weight;
                }
            }
            return profit;
        }

        Map<String, Integer> stationCapacity = new HashMap<>();
        if (player.getTrains() != null) {
            for (Train t : player.getTrains()) {
                String sUid = t.getStationUid();
                if (sUid == null)
                    continue;
                int cap = t.getCapacity() == null ? 0 : t.getCapacity().intValue();
                stationCapacity.put(sUid, stationCapacity.getOrDefault(sUid, 0) + cap);
            }
        }

        var map = mapService.getMap();

        for (Station s : player.getStations()) {
            int capacity = stationCapacity.getOrDefault(s.getUId(), 0);

            Integer customers = 0;
            if (map != null && map.getMap() != null) {
                outer: for (var row : map.getMap()) {
                    for (var field : row) {
                        var loc = field.getLocation();
                        if (loc != null && loc.getStation() != null && loc.getStation().getUId() != null
                                && loc.getStation().getUId().equals(s.getUId())) {
                            customers = loc.getCustomers();
                            break outer;
                        }
                    }
                }
            }

            int served = Math.min(customers == null ? 0 : customers, capacity);
            int fibIndex = Math.min(12, Math.max(0, served));
            int weight = fib(fibIndex + 1);
            int ticks = Math.max(1, (int) (seconds / RESOURCE_GENERATION_INTERVAL_IN_SECONDS));
            profit += ticks * weight;
            long timeBucket = System.currentTimeMillis() / (RESOURCE_GENERATION_INTERVAL_IN_SECONDS * 1000L);
            int noise = deterministicNoise(s.getUId(), timeBucket, 8);
            profit += noise * ticks;
        }

        return profit;
    }

    private Integer calculateRails(List<Rail> railList, Long seconds) {
        if (railList == null || railList.isEmpty()) return 0;

        Integer profit = 0;

        for (Rail rail : railList) {
            int lvl = rail.getLevel() == null ? 0 : rail.getLevel().intValue();
            int weight = fib(Math.min(12, lvl + 1));
            int ticksRail = Math.max(1, (int) (seconds / RESOURCE_GENERATION_INTERVAL_IN_SECONDS));
            profit += ticksRail * weight;
            long timeBucketRail = System.currentTimeMillis() / (RESOURCE_GENERATION_INTERVAL_IN_SECONDS * 1000L);
            int railNoise = deterministicNoise(rail.getUId(), timeBucketRail, 6);
            profit += railNoise * ticksRail;
        }

        return profit;
    }

    private Integer calculateStation(List<Station> stationList, Long seconds) {
        if (stationList == null || stationList.isEmpty()) return 0;

        Integer profit = 0;

        for (Station station : stationList) {
            int lvl = station.getLevel() == null ? 0 : station.getLevel().intValue();
            int weight = fib(Math.min(12, lvl + 2));
            int ticksStation = Math.max(1, (int) (seconds / RESOURCE_GENERATION_INTERVAL_IN_SECONDS));
            profit += ticksStation * weight;
            long timeBucketStation = System.currentTimeMillis() / (RESOURCE_GENERATION_INTERVAL_IN_SECONDS * 1000L);
            int stationNoise = deterministicNoise(station.getUId(), timeBucketStation, 6);
            profit += stationNoise * ticksStation;
        }

        return profit;
    }

    private int fib(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            int c = a + b;
            a = b;
            b = c;
        }
        return b;
    }

    private int deterministicNoise(String id, long timeBucket, int range) {
        if (id == null) id = "-";
        String key = id + "|" + Long.toString(timeBucket);
        int h = key.hashCode();
        int mod = Math.abs(h) % (range + 1);
        return mod;
    }
}
