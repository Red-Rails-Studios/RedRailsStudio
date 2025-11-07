package de.gts.redrail.game.component;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import static de.gts.redrail.game.constants.ResourceCost.NEW_RAIL;
import static de.gts.redrail.game.constants.ResourceCost.NEW_STATION;
import static de.gts.redrail.game.constants.ResourceCost.NEW_TRAIN;
import static de.gts.redrail.game.constants.ResourceCost.UPGRADE_RAIL_FACTOR;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_MAX_LEVEL_REACHED;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_NO_MATCH_PLAY_COMPONENT;
import static de.gts.redrail.game.constants.ResponseText.BOUGHT_NEW_PLAY_COMPONENT;
import static de.gts.redrail.game.constants.ResponseText.BOUGHT_UPGRADE;
import static de.gts.redrail.game.constants.ResponseText.CANT_AFFORD_NEW_PLAY_COMPONENT;
import static de.gts.redrail.game.constants.ResponseText.CANT_AFFORD_UPGRADE_PLAY_COMPONENT;
import static de.gts.redrail.game.constants.ResponseText.NOT_ENOUGH_EMPLOYEES;
import static de.gts.redrail.game.constants.ResponseText.REQIUERMENT_NOT_MET;
import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.utils.RailUtil;
import de.gts.redrail.game.utils.StationUtil;
import de.gts.redrail.game.utils.TrainUtil;
import lombok.RequiredArgsConstructor;
import de.gts.redrail.game.models.entities.Map;

@Component
@RequiredArgsConstructor
public class PlayComponentsStore {

    private final ResourceValidator resourceValidator;
    private final Map map;

    public ActionResult buyRail(Player player) {
        if (!resourceValidator.canBuyNewRail(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT, null);
        }

        Rail rail = new Rail();
        rail.setUId(UUID.randomUUID().toString());
        rail.setLevel(1);
        player.getRails().add(rail);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (player.getRails().size() * NEW_RAIL));

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT, rail.getUId());
    }

    public ActionResult upgradeRail(Player player, String railUid) {
        if (!resourceValidator.canUpgradeRail(player, railUid)) {
            return new ActionResult(false, CANT_AFFORD_UPGRADE_PLAY_COMPONENT);
        }

        Optional<Rail> railOptional = RailUtil.getRailByUid(player.getRails(), railUid);

        if (railOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAY_COMPONENT);
        }

        Rail rail = railOptional.get();

        if (rail.getLevel() >= 5) {
            return new ActionResult(false, ACTION_FAILED_MAX_LEVEL_REACHED);
        }

        Integer level = rail.getLevel() + 1;
        rail.setLevel(level);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }

    public ActionResult buyStation(Player player) {
        if (!resourceValidator.canBuyNewStation(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT, null);
        }

        Station station = findNearestStationLocation(player);
        station.setUId(UUID.randomUUID().toString());
        station.setLevel(1);
        player.getStations().add(station);
        station.setMasterUid(player.getUId());

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - NEW_STATION);

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT, station.getUId());
    }

    public Station findNearestStationLocation(Player player) {
        java.util.List<java.util.List<de.gts.redrail.game.models.entities.Field>> mapRows = map.getMap();

        // Collect candidate unassigned station locations
        java.util.List<de.gts.redrail.game.models.entities.Location> candidates = new java.util.ArrayList<>();
        java.util.List<de.gts.redrail.game.models.entities.Location> playerLocations = new java.util.ArrayList<>();

        for (java.util.List<de.gts.redrail.game.models.entities.Field> row : mapRows) {
            for (de.gts.redrail.game.models.entities.Field field : row) {
                de.gts.redrail.game.models.entities.Location loc = field.getLocation();
                if (loc == null)
                    continue;

                // any location may host a station; select unassigned ones
                // Map generation typically creates a blank Station object for each Location.
                // Consider locations unassigned if station is null or station UID is blank.
                if (loc.getStation() == null || loc.getStation().getUId() == null
                        || loc.getStation().getUId().isEmpty()) {
                    candidates.add(loc);
                } else {
                    // location already has a station assigned; if it's the player's station, add to
                    // playerLocations
                    if (player.getStations() != null) {
                        for (Station s : player.getStations()) {
                            if (s != null && s.getUId() != null && s.getUId().equals(loc.getStation().getUId())) {
                                playerLocations.add(loc);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // If no candidates, fallback to a new Station (unlikely)
        if (candidates.isEmpty()) {
            return new Station();
        }

        // Find candidate with minimal distance to any player's existing station
        // location
        de.gts.redrail.game.models.entities.Location best = null;
        long bestDist = Long.MAX_VALUE;

        for (de.gts.redrail.game.models.entities.Location candidate : candidates) {
            for (de.gts.redrail.game.models.entities.Location pLoc : playerLocations) {
                if (candidate.getX() == null || candidate.getY() == null || pLoc.getX() == null || pLoc.getY() == null)
                    continue;
                long dx = candidate.getX() - pLoc.getX();
                long dy = candidate.getY() - pLoc.getY();
                long dist2 = dx * dx + dy * dy;
                if (dist2 < bestDist) {
                    bestDist = dist2;
                    best = candidate;
                }
            }
        }

        if (best == null) {
            best = candidates.get(0);
        }

        // Return the existing Station instance on the Location (create one only if
        // missing)
        if (best.getStation() == null) {
            best.setStation(new Station());
        }
        return best.getStation();
    }

    public ActionResult upgradeStation(Player player, String stationUid) {
        if (!resourceValidator.canUpgradeStation(player, stationUid)) {
            return new ActionResult(false, CANT_AFFORD_UPGRADE_PLAY_COMPONENT);
        }

        Optional<Station> stationOptional = StationUtil.getStationByUid(player.getStations(), stationUid);

        if (stationOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAY_COMPONENT);
        }

        if (resourceValidator.getFreeEmployees(player) < stationOptional.get().getRequiredEmployees() + 2) {
            return new ActionResult(false, NOT_ENOUGH_EMPLOYEES);

        }

        if (stationOptional.get().getLevel() >= 10) {
            return new ActionResult(false, ACTION_FAILED_MAX_LEVEL_REACHED);
        }

        Station station = stationOptional.get();
        Integer level = station.getLevel() + 1;
        Integer trainCapacity = station.getTrainCapacity() + 2; 
        Integer railCapacity = station.getRailCapacity() + 1; 
        station.setTrainCapacity(trainCapacity);
        station.setRailCapacity(railCapacity);
        station.setLevel(level);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }

    public ActionResult buyTrain(Player player) {
        if (!resourceValidator.canBuyNewTrain(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT, null);
        }

        if (!resourceValidator.requirmentsForTrain(player)) {
            return new ActionResult(false, REQIUERMENT_NOT_MET, null);
        }

        Train train = new Train();
        train.setUId(UUID.randomUUID().toString());
        train.setLevel(1);
        player.getTrains().add(train);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - NEW_TRAIN);

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT, train.getUId());
    }

    public ActionResult upgradeTrain(Player player, String trainUid) {
        if (!resourceValidator.canUpgradeTrain(player, trainUid)) {
            return new ActionResult(false, CANT_AFFORD_UPGRADE_PLAY_COMPONENT);
        }

        Optional<Train> trainOptional = TrainUtil.getTrainByUid(player.getTrains(), trainUid);

        if (trainOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAY_COMPONENT);
        }

        Train train = trainOptional.get();

        if (train.getLevel() >= 10) {
            return new ActionResult(false, ACTION_FAILED_MAX_LEVEL_REACHED);
        }

        Integer level = train.getLevel() + 1;
        Integer capacity = train.getCapacity() + 2;
        train.setLevel(level);
        train.setCapacity(capacity);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }
}
