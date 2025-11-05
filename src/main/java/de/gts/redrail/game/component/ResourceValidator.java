package de.gts.redrail.game.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import static de.gts.redrail.game.constants.ResourceCost.NEW_RAIL;
import static de.gts.redrail.game.constants.ResourceCost.NEW_STATION;
import static de.gts.redrail.game.constants.ResourceCost.NEW_TRAIN;
import static de.gts.redrail.game.constants.ResourceCost.UPGRADE_RAIL_FACTOR;
import static de.gts.redrail.game.constants.ResourceCost.UPGRADE_STATION_FACTOR;
import static de.gts.redrail.game.constants.ResourceCost.UPGRADE_TRAIN_FACTOR;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.models.entities.UpgradeRequirements;
import de.gts.redrail.game.utils.RailUtil;
import de.gts.redrail.game.utils.StationUtil;
import de.gts.redrail.game.utils.TrainUtil;

@Component
public class ResourceValidator {
    // Default values for new components
    private static final Integer DEFAULT_TRAIN_REQUIRED_EMPLOYEES = 2;
    private static final Integer DEFAULT_TRAIN_REQUIRED_POWER = 1;
    private static final Integer DEFAULT_STATION_REQUIRED_EMPLOYEES = 1;
    private static final Integer DEFAULT_STATION_REQUIRED_POWER = 4;

    public boolean canBuyNewRail(Player player) {
        return player.getResourceRack().getDbCoin() >= NEW_RAIL;
    }

    /**
     * Build upgrade requirements for all upgradable objects of the player: trains,
     * stations and rails.
     * Returns an empty list if none available.
     */
    public List<UpgradeRequirements> getUpgradeRequirements(Player player) {
        List<UpgradeRequirements> requirements = new ArrayList<>();

        if (player == null) {
            return requirements;
        }

        // Trains
        if (player.getTrains() != null) {
            for (Train train : player.getTrains()) {
                if (train == null)
                    continue;
                if (train.getLevel() != null && train.getLevel() >= 10)
                    continue;

                int nextLevel = (train.getLevel() == null) ? 1 : (train.getLevel() + 1);
                int reqDb = nextLevel * UPGRADE_TRAIN_FACTOR;
                Integer trainReqPower = train.getRequiredPower();
                Integer trainReqEmployees = train.getRequiredEmployees();
                int reqPower = (trainReqPower == null) ? DEFAULT_TRAIN_REQUIRED_POWER : (trainReqPower.intValue() + 1);
                int reqEmployees = (trainReqEmployees == null) ? DEFAULT_TRAIN_REQUIRED_EMPLOYEES
                        : (trainReqEmployees.intValue() + 1);
                requirements.add(new UpgradeRequirements(reqDb, reqPower, reqEmployees, train.getUId()));
            }
        }

        // Stations
        if (player.getStations() != null) {
            for (Station station : player.getStations()) {
                if (station == null)
                    continue;
                if (station.getLevel() != null && station.getLevel() >= 10)
                    continue;

                int nextLevel = (station.getLevel() == null) ? 1 : (station.getLevel() + 1);
                int reqDb = nextLevel * UPGRADE_STATION_FACTOR;
                Integer stationReqPower = station.getRequiredPower();
                Integer stationReqEmployees = station.getRequiredEmployees();
                int reqPower = (stationReqPower == null) ? DEFAULT_STATION_REQUIRED_POWER
                        : (stationReqPower.intValue() + 1);
                int reqEmployees = (stationReqEmployees == null) ? DEFAULT_STATION_REQUIRED_EMPLOYEES
                        : (stationReqEmployees.intValue() + 1);
                requirements.add(new UpgradeRequirements(reqDb, reqPower, reqEmployees, station.getUId()));
            }
        }

        // Rails
        if (player.getRails() != null) {
            for (Rail rail : player.getRails()) {
                if (rail == null)
                    continue;
                if (rail.getLevel() != null && rail.getLevel() >= 10)
                    continue;

                int reqDb = (rail.getLevel() == null ? 1 : (rail.getLevel() + 1)) * UPGRADE_RAIL_FACTOR;
                // Rails don't require power/employees in current model
                requirements.add(new UpgradeRequirements(reqDb, 0, 0, rail.getUId()));
            }
        }

        return requirements;
    }

    /**
     * Returns the requirements for buying new components (train, station, rail).
     * UIdOfObjectToUpgrade is set to a descriptive constant string for the new
     * object.
     */
    public List<UpgradeRequirements> getBuyRequirements(Player player) {
        List<UpgradeRequirements> requirements = new ArrayList<>();

        // Train
        int trainReqDb = NEW_TRAIN;
        int trainReqPower = DEFAULT_TRAIN_REQUIRED_POWER;
        int trainReqEmployees = DEFAULT_TRAIN_REQUIRED_EMPLOYEES;
        requirements.add(new UpgradeRequirements(trainReqDb, trainReqPower, trainReqEmployees, "NEW_TRAIN"));

        // Station
        int stationReqDb = NEW_STATION;
        int stationReqPower = DEFAULT_STATION_REQUIRED_POWER;
        int stationReqEmployees = DEFAULT_STATION_REQUIRED_EMPLOYEES;
        requirements.add(new UpgradeRequirements(stationReqDb, stationReqPower, stationReqEmployees, "NEW_STATION"));

        // Rail (no power/employees required in current model)
        int railReqDb = NEW_RAIL;
        requirements.add(new UpgradeRequirements(railReqDb, 0, 0, "NEW_RAIL"));

        return requirements;
    }

    public boolean canUpgradeRail(Player player, String railUid) {
        Optional<Rail> railOptional = RailUtil.getRailByUid(player.getRails(), railUid);

        if (railOptional.isEmpty()) {
            return false;
        }

        return player.getResourceRack().getDbCoin() >= (railOptional.get().getLevel() + 1) * UPGRADE_RAIL_FACTOR;
    }

    public boolean requirmentsForTrain(Player player) {
        boolean anyStationHasCapacity = false;

        if (player.getStations() != null) {
            for (Station station : player.getStations()) {
                if (station != null && station.getTrainCapacity() != null && station.getTrainCapacity() > 0) {
                    anyStationHasCapacity = true;
                    break;
                }
            }
        }

        if (!anyStationHasCapacity) {
            return false;
        }

        if (getFreeEmployees(player) < DEFAULT_TRAIN_REQUIRED_EMPLOYEES) {
            return false; // Not enough free employees to buy a new train
        }

        if (getFreePower(player) < DEFAULT_TRAIN_REQUIRED_POWER) {
            return false; // Not enough free power to buy a new train

        }

        return player.getStations() != null && player.getStations().size() >= 2 && player.getRails() != null
                && player.getRails().size() >= player.getTrains().size() + 1;
    }

    public boolean canBuyNewTrain(Player player) {
        if (!requirmentsForTrain(player)) {
            return false;
        }
        return player.getResourceRack().getDbCoin() >= NEW_TRAIN;
    }

    public Integer getFreeEmployees(Player player) {
        Integer freeEmployees = player.getResourceRack().getEmployees();

        for (Train train : player.getTrains()) {
            freeEmployees -= train.getRequiredEmployees();
        }

        for (Station station : player.getStations()) {
            freeEmployees -= station.getRequiredEmployees();
        }

        return freeEmployees;
    }

    public Integer getFreePower(Player player) {
        Integer freePower = player.getResourceRack().getPower();

        for (Train train : player.getTrains()) {
            freePower -= train.getRequiredPower();
        }

        for (Station station : player.getStations()) {
            freePower -= station.getRequiredPower();
        }

        return freePower;
    }

    public boolean canUpgradeTrain(Player player, String trainUid) {
        Optional<Train> trainOptional = TrainUtil.getTrainByUid(player.getTrains(), trainUid);
        if (trainOptional.isEmpty()) {
            return false;
        }

        if (trainOptional.get().getLevel() >= 10) {
            return false; // Assuming level 10 is the max level for a train
        }

        if (getFreeEmployees(player) < trainOptional.get().getRequiredEmployees() + 1) {
            return false; // Not enough free employees to upgrade the train
        }

        if (getFreePower(player) < trainOptional.get().getRequiredPower() + 1) {
            return false; // Not enough free power to upgrade the train
        }

        return player.getResourceRack().getDbCoin() >= (trainOptional.get().getLevel() + 1) * UPGRADE_TRAIN_FACTOR;
    }

    public boolean canBuyNewStation(Player player) {
        if (player.getResourceRack().getEmployees() < player.getTrains().size() * DEFAULT_TRAIN_REQUIRED_EMPLOYEES
                + player.getStations().size() * DEFAULT_STATION_REQUIRED_EMPLOYEES
                || player.getResourceRack().getPower() < player.getStations().size() * DEFAULT_STATION_REQUIRED_POWER
                        + player.getTrains().size() * DEFAULT_TRAIN_REQUIRED_POWER) {
            return false;
        }

        if (getFreeEmployees(player) < DEFAULT_STATION_REQUIRED_EMPLOYEES) {
            return false; // Not enough free employees to buy a new station

        }

        if (getFreePower(player) < DEFAULT_STATION_REQUIRED_POWER) {
            return false; // Not enough free power to buy a new station
        }

        return player.getResourceRack().getDbCoin() >= NEW_STATION;
    }

    public boolean canUpgradeStation(Player player, String stationUid) {
        Optional<Station> stationOptional = StationUtil.getStationByUid(player.getStations(), stationUid);
        if (stationOptional.isEmpty()) {
            return false;
        }

        if (getFreeEmployees(player) < stationOptional.get().getRequiredEmployees() + 1) {
            return false; 
        }

        return player.getResourceRack().getDbCoin() >= (stationOptional.get().getLevel() + 1) * UPGRADE_STATION_FACTOR;
    }

}
