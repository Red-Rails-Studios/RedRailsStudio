package de.gts.redrail.game.component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

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

        // Station-level generation (unchanged)
        dbCoins += calculateStation(player.getStations(), seconds);

        // Train generation: limited by assigned location customers per station
        dbCoins += calculateTrainIncomeByStation(player, seconds);

        // Rail income unchanged
        dbCoins += calculateRails(player.getRails(), seconds);

        player.getResourceRack().setDbCoin(dbCoins);
    }

    private Integer calculateTrainIncomeByStation(Player player, Long seconds) {
        int profit = 0;

        if (player.getStations() == null || player.getStations().isEmpty()) {
            // fallback: sum all trains as before
            if (player.getTrains() != null) {
                    for (Train t : player.getTrains()) {
                int cap = t.getCapacity() == null ? 0 : t.getCapacity().intValue();
                int fibIndex = Math.min(12, Math.max(0, cap));
                int weight = fib(fibIndex + 1);
                profit += seconds.intValue() * weight;
                    }
            }
            return profit;
        }

        // Build a quick map stationUid -> total capacity of trains assigned to that station
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

        // For each station, find assigned location customers via map and compute served customers
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
            // Use a Fibonacci-based weight for less 'round' numbers. Cap the index to avoid huge values.
            int fibIndex = Math.min(12, Math.max(0, served));
            int weight = fib(fibIndex + 1); // shift index to avoid fib(0)=0
            profit += seconds.intValue() * weight;
        }

        return profit;
    }

    private Integer calculateRails(List<Rail> railList, Long seconds) {
        if (railList == null || railList.isEmpty()) return 0;

        Integer profit = 0;

        for (Rail rail : railList) {
            int lvl = rail.getLevel() == null ? 0 : rail.getLevel().intValue();
            int weight = fib(Math.min(12, lvl + 1));
            profit += seconds.intValue() * weight;
        }

        return profit;
    }

    private Integer calculateStation(List<Station> stationList, Long seconds) {
        if (stationList == null || stationList.isEmpty()) return 0;

        Integer profit = 0;

        for (Station station : stationList) {
            int lvl = station.getLevel() == null ? 0 : station.getLevel().intValue();
            int weight = fib(Math.min(12, lvl + 2)); // shift so level 1 maps to fib(3)
            profit += seconds.intValue() * weight;
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
}
