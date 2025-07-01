package de.gts.redrail.game.component;

import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;

import de.gts.redrail.game.utils.StationUtil;
import de.gts.redrail.game.utils.TrainUtil;
import de.gts.redrail.game.utils.RailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static de.gts.redrail.game.constants.ResourceCost.NEW_RAIL;
import static de.gts.redrail.game.constants.ResourceCost.UPGRADE_RAIL_FACTOR;
import static de.gts.redrail.game.constants.ResponseText.*;

@Component
@RequiredArgsConstructor
public class PlayComponentsStore {

    private final ResourceValidator resourceValidator;

    public ActionResult buyRail(Player player) {
        if (!resourceValidator.canBuyNewRail(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT);
        }

        Rail rail = new Rail();

        rail.setUId(UUID.randomUUID().toString());
        rail.setLevel(1);

        player.getRails().add(rail);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (player.getRails().size()*NEW_RAIL)); 

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT);
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
        Integer level = rail.getLevel() + 1;
        rail.setRequiredTime(rail.getRequiredTime() - 1); 
        rail.setLevel(level);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }

    public ActionResult buyStation (Player player) {
        if (!resourceValidator.canBuyNewStation(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT);
        }

        Station station = new Station();

        station.setUId(UUID.randomUUID().toString());
        station.setLevel(1);

        player.getStations().add(station);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - NEW_RAIL);

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT);
    }

    public ActionResult upgradeStation(Player player, String stationUid) {
        if (!resourceValidator.canUpgradeStation(player, stationUid)) {
            return new ActionResult(false, CANT_AFFORD_UPGRADE_PLAY_COMPONENT);
        }

        Optional<Station> stationOptional = StationUtil.getStationByUid(player.getStations(), stationUid);

        if (stationOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAY_COMPONENT);
        }
        if(resourceValidator.getFreeEmployees(player) < stationOptional.get().getRequierdEmployes()+2) {
            return new ActionResult(false, NOT_ENOUGH_EMPLOYEES);
            
        }
        if(stationOptional.get().getLevel() >= 10) {
            return new ActionResult(false, ACTION_FAILED_MAX_LEVEL_REACHED);
        }

        Station station = stationOptional.get();
        Integer level = station.getLevel() + 1;
        Integer trainCapacity = station.getTrainCapacity() + 2; // Assuming each upgrade increases train capacity by 1
        Integer railCapacity = station.getRailCapacity() + 2; // Assuming each upgrade increases rail capacity by 1
        station.setTrainCapacity(trainCapacity);
        station.setRailCapacity(railCapacity);
        station.setLevel(level);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }


    public ActionResult buyTrain(Player player) {
        if (!resourceValidator.canBuyNewTrain(player)) {
            return new ActionResult(false, CANT_AFFORD_NEW_PLAY_COMPONENT);
        }
        if (!resourceValidator.requirmentsForTrain(player)) {
            return new ActionResult(false, REQIUERMENT_NOT_MET);
        }

        Train train = new Train();

        train.setUId(UUID.randomUUID().toString());
        train.setLevel(1);

        player.getTrains().add(train);

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - NEW_RAIL);

        return new ActionResult(true, BOUGHT_NEW_PLAY_COMPONENT);
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
        Integer level = train.getLevel() + 1;
        Integer capacity = train.getCapacity() + 2; 
        train.setLevel(level);
        train.setCapacity(capacity);
    

        Integer dbCoin = player.getResourceRack().getDbCoin();
        player.getResourceRack().setDbCoin(dbCoin - (level * UPGRADE_RAIL_FACTOR));

        return new ActionResult(true, BOUGHT_UPGRADE);
    }
}
