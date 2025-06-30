package de.gts.redrail.game.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import de.gts.redrail.game.component.GameClock;
import de.gts.redrail.game.component.PlayComponentsStore;
import de.gts.redrail.game.component.ResourceCalculator;
import de.gts.redrail.game.constants.GameStateEnum;
import static de.gts.redrail.game.constants.GameStateEnum.FINISHED;
import static de.gts.redrail.game.constants.GameStateEnum.NOT_CREATED;
import static de.gts.redrail.game.constants.GameStateEnum.NOT_STARTED;
import static de.gts.redrail.game.constants.GameStateEnum.RUNNING;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_NO_MATCH_PLAYER;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_NO_TRAIN_CAPACITY_LEFT;
import de.gts.redrail.game.mappers.dtos.PlayerDtoMapper;
import de.gts.redrail.game.mappers.dtos.PlayerOverviewDtoMapper;
import de.gts.redrail.game.mappers.entities.PlayerMapper;
import de.gts.redrail.game.models.dtos.PlayerDto;
import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import de.gts.redrail.game.models.dtos.SessionOverviewDto;
import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.utils.PlayerUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private String sessionName;
    private final GameClock sessionClock;
    private List<Player> sessionPlayers;
    private GameStateEnum gameState = NOT_CREATED;
    private final ResourceCalculator resourceCalculator;
    private final PlayComponentsStore playComponentsStore;

    private final PlayerDtoMapper playerDtoMapper;
    private final PlayerMapper playerMapper;
    private final PlayerOverviewDtoMapper playerOverviewDtoMapper;

    public void createSession(String name) {
        sessionName = name;
        sessionPlayers = new ArrayList<>();
        gameState = NOT_STARTED;
    }
    public void killSession() {
        sessionName = null;
        sessionPlayers = null;
        gameState = NOT_CREATED;
        sessionClock.endClock();
    }
    public List<PlayerOverviewDto> getPlayers() {
        if (!gameState.equals(RUNNING)) {
            throw new IllegalStateException("get players failed - session is not running");
        }

        return playerOverviewDtoMapper.map(sessionPlayers);
    }
    public List<PlayerDto> getPlayers2() {
        if (!gameState.equals(RUNNING)) {
            throw new IllegalStateException("get players failed - session is not running");
        }

        List<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player : sessionPlayers) {
            playerDtos.add(playerDtoMapper.map(player));
        }
        return playerDtos;
    }

    public boolean startSession() {
        if (CollectionUtils.isEmpty(sessionPlayers)) {
            return false;
        }

        gameState = RUNNING;
        sessionClock.startClock();

        return true;
    }

    public long endSession() {
        gameState = FINISHED;
        sessionClock.endClock();
    return Duration.between(sessionClock.getStarted(), sessionClock.getEnded()).toMinutes();
    }

    public SessionOverviewDto createCurrentSessionOverview() {
        SessionOverviewDto sessionOverviewDto = new SessionOverviewDto();

        sessionOverviewDto.setSessionName(sessionName);
        sessionOverviewDto.setPlayers(playerOverviewDtoMapper.map(sessionPlayers));
        sessionOverviewDto.setGameState(gameState);
        sessionOverviewDto.setSessionStarted(sessionClock.getStarted());
        sessionOverviewDto.setSessionEnded(sessionClock.getEnded());

        return sessionOverviewDto;
    }

    public boolean joinSession(PlayerOverviewDto playerWantToJoin) {
        for (Player player : sessionPlayers) {
            if (PlayerUtil.isPlayerMatching(player, playerWantToJoin)) {
                return false;
            }
        }

        Player newPlayer = playerMapper.map(playerWantToJoin);

        if (newPlayer == null) {
            return false;
        }

        Rail rail = new Rail();
        Station station1 = new Station();
        Station station2 = new Station();
        Train train = new Train();
        rail.setUId(UUID.randomUUID().toString());
        rail.setLevel(1);

        station1.setUId(UUID.randomUUID().toString());
        station1.setLevel(1);
        station2.setUId(UUID.randomUUID().toString());
        station2.setLevel(1);
        
        train.setUId(UUID.randomUUID().toString());
        train.setLevel(1);
        station1.setTrainCapacity(station1.getTrainCapacity() - 1);
       
        newPlayer.setRails(new ArrayList<>());
        newPlayer.getRails().add(rail);

        newPlayer.setStations(new ArrayList<>());
        newPlayer.getStations().add(station1);
        newPlayer.getStations().add(station2);

        newPlayer.setTrains(new ArrayList<>());
        newPlayer.getTrains().add(train);

        sessionPlayers.add(newPlayer);

        return true;
    }

    public PlayerDto getPlayerStatus(String uId) {
        resourceCalculator.calculateResource(sessionPlayers);

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, uId);

        if (playerOptional.isEmpty()) {
            return null;
        }

        return playerDtoMapper.map(playerOptional.get());
    }

    public ActionResult buyRail(String playerUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()));

        return playComponentsStore.buyRail(playerOptional.get());
        
    }

    public ActionResult upgradeRail(String playerUid, String railUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()));

        return playComponentsStore.upgradeRail(playerOptional.get(), railUid);
    }
    
    public ActionResult buyStation(String playerUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()));

        return playComponentsStore.buyStation(playerOptional.get());
    }

    public ActionResult upgradeStation(String playerUid, String stationUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()));

        return playComponentsStore.upgradeStation(playerOptional.get(), stationUid);
    }

    public ActionResult buyTrain(String playerUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }
       

        resourceCalculator.calculateResource(List.of(playerOptional.get()));
        for(Station station : playerOptional.get().getStations()) {
            if (station.getTrainCapacity() == 0) {
                return new ActionResult(false, ACTION_FAILED_NO_TRAIN_CAPACITY_LEFT);
            }
            else {
                station.setTrainCapacity(station.getTrainCapacity() - 1);
            }
        }

        
        return playComponentsStore.buyTrain(playerOptional.get());
    }

    public ActionResult upgradeTrain(String playerUid, String trainUid) {
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionPlayers, playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }
        

        resourceCalculator.calculateResource(List.of(playerOptional.get()));

        return playComponentsStore.upgradeTrain(playerOptional.get(), trainUid);
    }

    public boolean isSessionNameMatching(String name) {
        if (name == null || sessionName == null) {
            return false;
        }

        return sessionName.equals(name);
    }

    public GameStateEnum getGameState() {
        return gameState;
    }
}
