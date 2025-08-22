package de.gts.redrail.game.component;

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

    public boolean canUpgradeRail(Player player, String railUid) {
        Optional<Rail> railOptional = RailUtil.getRailByUid(player.getRails(), railUid);

        if (railOptional.isEmpty()) {
            return false;
        }

        return player.getResourceRack().getDbCoin() >= (railOptional.get().getLevel() + 1) * UPGRADE_RAIL_FACTOR;
    }

    public boolean requirmentsForTrain(Player player) {
        
        for(Station station : player.getStations()) {
            if (station.getTrainCapacity() == 0)
              {      
                return false;
              }
        }

       if (getFreeEmployees(player) < DEFAULT_TRAIN_REQUIRED_EMPLOYEES) {
            return false; // Not enough free employees to buy a new train
        }

        if (getFreePower(player) < DEFAULT_TRAIN_REQUIRED_POWER) {
            return false; // Not enough free power to buy a new train
            
        }
       

        return player.getStations().size() >= 2 && player.getRails().size() >= player.getTrains().size() + 1;
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
            freeEmployees -= station.getRequierdEmployes();
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
        if(player.getResourceRack().getEmployees() < player.getTrains().size() * DEFAULT_TRAIN_REQUIRED_EMPLOYEES + player.getStations().size() * DEFAULT_STATION_REQUIRED_EMPLOYEES || player.getResourceRack().getPower() < player.getStations().size() * DEFAULT_STATION_REQUIRED_POWER + player.getTrains().size() * DEFAULT_TRAIN_REQUIRED_POWER) {
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

        if (getFreeEmployees(player) < stationOptional.get().getRequierdEmployes() + 1) {
            return false; // Not enough free employees to upgrade the train
        }


        return player.getResourceRack().getDbCoin() >= (stationOptional.get().getLevel() + 1) * UPGRADE_STATION_FACTOR;
    }
    
}
