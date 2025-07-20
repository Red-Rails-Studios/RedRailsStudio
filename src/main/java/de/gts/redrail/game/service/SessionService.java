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
import de.gts.redrail.game.models.entities.SessionData;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.utils.PlayerUtil;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class SessionService {

    private String sessionName;
    private List<Player> sessionPlayers;
    private GameStateEnum gameState = NOT_CREATED;
    private final ResourceCalculator resourceCalculator;
    private final PlayComponentsStore playComponentsStore;

    private final PlayerDtoMapper playerDtoMapper;
    private final PlayerMapper playerMapper;
    private final PlayerOverviewDtoMapper playerOverviewDtoMapper;
    private final List<SessionData> sessions = new ArrayList<>();

    public SessionOverviewDto createSession(String name) { 
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Session name cannot be null or blank");
        }

        if (findSessionByName(name) != null) {
            throw new IllegalArgumentException("Session with this name already exists");
        }

        SessionData sessionData = new SessionData();
        sessionData.setSessionName(name);
        sessionData.setGameState(GameStateEnum.NOT_STARTED);
        sessionData.setSessionClock(new GameClock());
        sessionData.setSessionPlayers(new ArrayList<>()); 
        sessions.add(sessionData);
        
        return createSessionOverview(sessionData.getSessionName());
    }

    public void killSession(String sessionName) {
        sessionName = null;
        sessionPlayers = null;
        gameState = NOT_CREATED;
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData != null && sessionData.getSessionClock() != null) {
            sessionData.getSessionClock().endClock();
            sessionData.getSessionClock().setStarted(null);
            sessionData.getSessionClock().setEnded(null);
        }

        for (SessionData session : sessions) {
            if (session.getSessionName().equals(sessionName)) {
                sessions.remove(session);
                break;
            }
        }
    }

    public boolean isSessionNameMatching(String name) {
        return findSessionByName(name) != null;
    }

    public GameStateEnum getGameState(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        return sessionData != null ? sessionData.getGameState() : NOT_CREATED;
    }

    public List<PlayerOverviewDto> getAllPlayerOverview(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || !sessionData.getGameState().equals(RUNNING)) {
            throw new IllegalStateException("get players failed - session is not running");
        }
        return playerOverviewDtoMapper.map(sessionData.getSessionPlayers());
    }

    public List<PlayerDto> getAllPlayer(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || !sessionData.getGameState().equals(RUNNING)) {
            throw new IllegalStateException("get players failed - session is not running");
        }
        List<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player : sessionData.getSessionPlayers()) {
            playerDtos.add(playerDtoMapper.map(player));
        }
        return playerDtos;
    }

    public boolean startSession(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || CollectionUtils.isEmpty(sessionData.getSessionPlayers())) {
            return false;
        }
        sessionData.setGameState(RUNNING);
        if (sessionData.getSessionClock() == null) {
            sessionData.setSessionClock(new GameClock());
        }
        sessionData.getSessionClock().startClock();
        return true;
    }

    public long endSession(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || sessionData.getSessionClock() == null) {
            return 0;
        }

        sessionData.setGameState(FINISHED);
        sessionData.getSessionClock().endClock();

        for (SessionData session : sessions) {
            if (session.getSessionName().equals(sessionName)) {
                sessions.remove(session);
                break;
            }
        }

        return Duration.between(sessionData.getSessionClock().getStarted(), sessionData.getSessionClock().getEnded()).toMinutes();
    }

    public SessionOverviewDto createCurrentSessionOverview() {
        SessionOverviewDto sessionOverviewDto = new SessionOverviewDto();
        sessionOverviewDto.setSessionName(sessionName);
        sessionOverviewDto.setPlayers(playerOverviewDtoMapper.map(sessionPlayers));
        sessionOverviewDto.setGameState(gameState);
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData != null && sessionData.getSessionClock() != null) {
            sessionOverviewDto.setSessionStarted(sessionData.getSessionClock().getStarted());
            sessionOverviewDto.setSessionEnded(sessionData.getSessionClock().getEnded());
        } else {
            sessionOverviewDto.setSessionStarted(null);
            sessionOverviewDto.setSessionEnded(null);
        }
        return sessionOverviewDto;
    }

    public boolean joinSession(PlayerOverviewDto playerWantToJoin, String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return false;
        }

        for (Player player : sessionData.getSessionPlayers()) {
            if (PlayerUtil.isPlayerMatching(player, playerWantToJoin)) {
                return false;
            }
        }

        if (sessionData.getGameState() != NOT_STARTED) {
            return false;
        }

        Player newPlayer = playerMapper.map(playerWantToJoin);
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
        sessionData.getSessionPlayers().add(newPlayer);
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

    public List<Player> getRanking() {
        
        if (gameState.equals(NOT_CREATED) || gameState.equals(NOT_STARTED)) {
            throw new IllegalStateException("get ranking failed - session is not created or not started");
        }

        List<Player> playerDtos = new ArrayList<>(sessionPlayers);
        List<Player> sortedPlayers = new ArrayList<>();
        
        for(Player p : playerDtos)
        {

            for (Station s : p.getStations()){
                p.setPoints(p.getPoints() + (3 + (2 * s.getLevel())));
            }

            for (Train t : p.getTrains()){
                p.setPoints(p.getPoints() + (int) (2 + (1.5 * t.getLevel())));
            }

            for (Rail r : p.getRails()){
                p.setPoints(p.getPoints() + (int) (1 +  r.getLevel()));
            }

            p.setPoints(p.getPoints() + (int) (p.getResourceRack().getEmployees() / 2));
            p.setPoints(p.getPoints() + (int) (p.getResourceRack().getPower() / 2));

        }

        for (Player p : playerDtos) {

            for (int i = 0; i < sortedPlayers.size(); i++) {

                if (p.getPoints() > sortedPlayers.get(i).getPoints()) {
                    sortedPlayers.add(i, p);
                    break;
                }
            }
        }

        return sortedPlayers;
    }

    public List<SessionOverviewDto> getAllSessionsOverview() {
        List<SessionOverviewDto> overviewList = new ArrayList<>();
        for (SessionData sessionData : sessions) {
            overviewList.add(createSessionOverview(sessionData.getSessionName()));
        }

        return overviewList;
    }

    public SessionOverviewDto createSessionOverview(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            throw new IllegalArgumentException("Session with name " + sessionName + " does not exist");
        }

        SessionOverviewDto dto = new SessionOverviewDto();
        dto.setSessionName(sessionData.getSessionName());
        if (playerOverviewDtoMapper != null) {
            dto.setPlayers(playerOverviewDtoMapper.map(sessionData.getSessionPlayers()));
        } else {
            dto.setPlayers(new ArrayList<>());
        }
        dto.setGameState(sessionData.getGameState());
        dto.setSessionStarted(sessionData.getSessionClock().getStarted());
        dto.setSessionEnded(sessionData.getSessionClock().getEnded());
        return dto;
    }

    private SessionData findSessionByName(String name) {
        for (SessionData session : sessions) {
            if (session.getSessionName().equals(name)) {
                return session;
            }
        }
        return null;
    }
}
