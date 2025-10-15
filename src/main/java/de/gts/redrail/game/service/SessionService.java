package de.gts.redrail.game.service;

import java.time.OffsetDateTime;
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
import static de.gts.redrail.game.constants.ResourceCost.NEW_EMPLOYEES;
import static de.gts.redrail.game.constants.ResourceCost.NEW_POWER;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_NO_MATCH_PLAYER;
import static de.gts.redrail.game.constants.ResponseText.ACTION_FAILED_NO_TRAIN_CAPACITY_LEFT;
import de.gts.redrail.game.mappers.dtos.PlayerDtoMapper;
import de.gts.redrail.game.mappers.dtos.PlayerOverviewDtoMapper;
import de.gts.redrail.game.mappers.dtos.TrainDtoMapper;
import de.gts.redrail.game.mappers.entities.PlayerMapper;
import de.gts.redrail.game.models.dtos.PlayerDto;
import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import de.gts.redrail.game.models.dtos.SessionOverviewDto;
import de.gts.redrail.game.models.dtos.TrainDto;
import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.Map;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.models.entities.Rail;
import de.gts.redrail.game.models.entities.SessionData;
import de.gts.redrail.game.models.entities.Station;
import de.gts.redrail.game.models.entities.Train;
import de.gts.redrail.game.models.entities.UpgradeRequirements;
import de.gts.redrail.game.utils.PlayerUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final ResourceCalculator resourceCalculator;
    private final PlayComponentsStore playComponentsStore;
    private final PlayerDtoMapper playerDtoMapper;
    private final PlayerMapper playerMapper;
    private final PlayerOverviewDtoMapper playerOverviewDtoMapper;
    private final MapSpaceGenerationService MapService;
    private final List<SessionData> sessions = new ArrayList<>();
    private final EventService eventService;
    private final de.gts.redrail.game.component.ResourceValidator resourceValidator;
    private final TrainDtoMapper trainDtoMapper;


    public List<SessionData> getAllSessions() {
        return sessions;
    }

    public Player findPlayerByUid(SessionData sessionData, String playerUid) {
        if (sessionData == null || playerUid == null) {
            return null;
        }

        for (Player player : sessionData.getSessionPlayers()) {
            if (player.getUId().equals(playerUid)) {
                return player;
            }
        }

        return null;
    }

    public ActionResult buyPower(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        // Check if player has enough coins to buy power
        if (playerOptional.get().getResourceRack().getDbCoin() < NEW_POWER) {
            return new ActionResult(false, "Not enough coins to buy power");
        }

        // Deduct cost and add power
        Integer dbCoin = playerOptional.get().getResourceRack().getDbCoin();
        playerOptional.get().getResourceRack().setDbCoin(dbCoin - NEW_POWER);
        playerOptional.get().getResourceRack().setPower(playerOptional.get().getResourceRack().getPower() + 5);

        return new ActionResult(true, "Power purchased successfully");
    }

    public ActionResult buyEmployees(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        // Check if player has enough coins to buy employees
        if (playerOptional.get().getResourceRack().getDbCoin() < NEW_EMPLOYEES) {
            return new ActionResult(false, "Not enough coins to buy employees");
        }

        // Deduct cost and add employees
        Integer dbCoin = playerOptional.get().getResourceRack().getDbCoin();
        playerOptional.get().getResourceRack().setDbCoin(dbCoin - NEW_EMPLOYEES);
        playerOptional.get().getResourceRack().setEmployees(playerOptional.get().getResourceRack().getEmployees() + 3);

        return new ActionResult(true, "Employees hired successfully");
    }

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

    // Trigger asynchronous map generation so GET /map doesn't block the caller.
    MapService.ensureGeneratedAsync();

    return createSessionOverview(sessionData.getSessionName());
    }

    public void killSession(String sessionName) {
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
        if (sessionData != null) {
            sessionData.setSessionName(null);
            sessionData.setSessionClock(null);
            sessionData.setSessionPlayers(new ArrayList<>());
            sessionData.setGameState(GameStateEnum.NOT_CREATED);
        }
    }

    public void killAllSessions() {
        List<SessionData> sessionsCopy = new ArrayList<>(sessions);
        for (SessionData session : sessionsCopy) {
            killSession(session.getSessionName());
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
        if (sessionData == null) {
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

        return sessionData.getSessionClock().getSessionDurationInMinutes();
    }

    public SessionOverviewDto createCurrentSessionOverview(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null; // or throw exception
        }

        SessionOverviewDto sessionOverviewDto = new SessionOverviewDto();
        sessionOverviewDto.setSessionName(sessionName);
        sessionOverviewDto.setPlayers(playerOverviewDtoMapper.map(sessionData.getSessionPlayers()));
        sessionOverviewDto.setGameState(sessionData.getGameState());
        sessionOverviewDto.setSessionStarted(sessionData.getSessionClock().getStarted());
        sessionOverviewDto.setSessionEnded(sessionData.getSessionClock().getEnded());

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
            // Check if player name matches the one trying to join
            if (player.getName().equalsIgnoreCase(playerWantToJoin.getName())) {
                return false;
            }
        } // Check if player already exists in the session

        if (sessionData.getGameState() != NOT_STARTED) {
            return false;
        }

        if (sessionData.getSessionPlayers().size() >= 4) {
            return false; // Maximum of 4 players allowed
        }

        Player newPlayer = playerMapper.map(playerWantToJoin);
        Rail rail = new Rail();
        Train train = new Train();
        rail.setUId(UUID.randomUUID().toString());
        rail.setLevel(1);
        // Determine corner index for this player (0..3) based on current number of players
        int cornerIndex = sessionData.getSessionPlayers().size();
        de.gts.redrail.game.models.entities.Location cornerLoc = findLocationAtCorner(cornerIndex);
        Station station1 = null;
        Station station2 = null;

        switch (cornerIndex) {
            case 1:
                newPlayer.setColor(de.gts.redrail.game.constants.Color.RED);
                break;
            case 2:
                newPlayer.setColor(de.gts.redrail.game.constants.Color.GREEN);
                break;
            case 3:
                newPlayer.setColor(de.gts.redrail.game.constants.Color.BLUE);
                break;
            case 4:
                newPlayer.setColor(de.gts.redrail.game.constants.Color.YELLOW);
                break;
        }

        if (cornerLoc != null) {
            station1 = cornerLoc.getStation();
        }

        if (station1 == null) {
            station1 = new Station();
        }

        // pick second station as the nearest unassigned station to the corner
        int baseX = 0;
        int baseY = 0;

        if (cornerLoc != null) {
            if (cornerLoc.getX() != null) baseX = cornerLoc.getX().intValue();
            if (cornerLoc.getY() != null) baseY = cornerLoc.getY().intValue();
        }

        de.gts.redrail.game.models.entities.Location nearest = findNearestUnassignedLocation(baseX, baseY, station1);

        if (nearest != null) {
            station2 = nearest.getStation();
        }

        if (station2 == null) {
            station2 = new Station();
        }

        // assign properties to the station instances (they may be blank from map)
        if (station1.getUId() == null || station1.getUId().isEmpty()) {
            station1.setUId(UUID.randomUUID().toString());
        }

        station1.setLevel(1);
        
        if (station2.getUId() == null || station2.getUId().isEmpty()) {
            station2.setUId(UUID.randomUUID().toString());
        }

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
        // Ensure these stations are linked to map locations (if they were created from map they already are)
        if (cornerLoc != null && cornerLoc.getStation() == null) {
            cornerLoc.setStation(station1);
        }
        if (nearest != null && nearest.getStation() == null) {
            nearest.setStation(station2);
        }
        return true;
    }

    // Return the Location representing one of the four corners
    private de.gts.redrail.game.models.entities.Location findLocationAtCorner(int cornerIndex) {
        Map map = MapService.getMap();
        if (map == null || map.getMap() == null)
            return null;

        int maxX = map.getMap().size() - 1;
        int maxY = map.getMap().get(0).size() - 1;

        switch (cornerIndex) {
            case 0: // top-left
                return map.getMap().get(0).get(0).getLocation();
            case 1: // top-right
                return map.getMap().get(0).get(maxY).getLocation();
            case 2: // bottom-left
                return map.getMap().get(maxX).get(0).getLocation();
            case 3: // bottom-right
                return map.getMap().get(maxX).get(maxY).getLocation();
            default:
                return map.getMap().get(0).get(0).getLocation();
        }
    }

    // Find nearest location without an assigned station (or with an unowned station) to the base coordinates.
    private de.gts.redrail.game.models.entities.Location findNearestUnassignedLocation(int baseX, int baseY, Station exclude) {
        Map map = MapService.getMap();
        if (map == null || map.getMap() == null)
            return null;

        de.gts.redrail.game.models.entities.Location best = null;
        double bestDist = Double.MAX_VALUE;

        for (var row : map.getMap()) {
            for (var field : row) {
                var loc = field.getLocation();
                if (loc == null)
                    continue;
                Station s = loc.getStation();
                // skip the explicitly excluded station
                if (s != null && exclude != null && s.getUId() != null && exclude.getUId() != null
                        && s.getUId().equals(exclude.getUId()))
                    continue;

                boolean available = (s == null) || (s.getUId() == null || s.getUId().isEmpty());
                if (!available)
                    continue;

                int lx = (loc.getX() == null) ? 0 : loc.getX().intValue();
                int ly = (loc.getY() == null) ? 0 : loc.getY().intValue();
                double dx = (double) lx - baseX;
                double dy = (double) ly - baseY;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < bestDist) {
                    bestDist = dist;
                    best = loc;
                }
            }
        }

        return best;
    }

    public boolean leaveSession(PlayerOverviewDto playerWantToLeave, String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return false;
        }

        // Remove player matching the given PlayerOverviewDto
        return sessionData.getSessionPlayers().removeIf(
                player -> PlayerUtil.isPlayerMatching(player, playerWantToLeave));
    }

    public PlayerOverviewDto getPlayerOverview(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        List<PlayerOverviewDto> players = playerOverviewDtoMapper.map(sessionData.getSessionPlayers());
        if (players.isEmpty()) {
            return null;
        }

        for (PlayerOverviewDto player : players) {
            if (player.getUId().equals(playerUid)) {
                return player;
            }
        }

        return null;
    }

    public List<TrainDto> getTrainsInfo(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        Player player = findPlayerByUid(sessionData, playerUid);
        if (player == null || player.getTrains() == null) {
            return null;
        }

        return trainDtoMapper.map(player.getTrains());
    }

    public PlayerDto getPlayerStatus(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        resourceCalculator.calculateResource(sessionData.getSessionPlayers(), sessionData.getSessionClock());
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return null;
        }

        return playerDtoMapper.map(playerOptional.get());
    }

    public ActionResult buyRail(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        return playComponentsStore.buyRail(playerOptional.get());

    }

    public ActionResult upgradeRail(String sessionName, String playerUid, String railUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        return playComponentsStore.upgradeRail(playerOptional.get(), railUid);
    }

    public ActionResult buyStation(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        ActionResult result = playComponentsStore.buyStation(playerOptional.get());
        if (result.isSuccessful()) {
            // find new station and assign to free location
            Optional<Station> stationOptional = playerOptional.get().getStations().stream()
                    .filter(s -> s.getUId().equals(result.getUid())).findFirst();
            stationOptional.ifPresent(s -> assignStationToAnyLocation(playerOptional.get(), s));
        }
        return result;
    }

    // Finds a free (unassigned) location on the global map and attaches the station to it.
    private void assignStationToAnyLocation(Player player, Station station) {
        Map map = MapService.getMap();
        if (map == null || map.getMap() == null || station == null)
            return;

        for (var row : map.getMap()) {
            for (var field : row) {
                var loc = field.getLocation();
                if (loc != null) {
                    var assigned = loc.getStation();
                    if (assigned == null || assigned.getUId() == null || assigned.getUId().isEmpty()) {
                        // assign
                        loc.setStation(station);
                        station.setMasterUID(player.getUId());
                        return;
                    }
                }
            }
        }
    }

    public ActionResult upgradeStation(String sessionName, String playerUid, String stationUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        return playComponentsStore.upgradeStation(playerOptional.get(), stationUid);
    }

    public ActionResult buyTrain(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        // validate there's at least one station capacity (ResourceValidator already partly checks this)
        boolean hasCapacity = false;
        for (Station station : playerOptional.get().getStations()) {
            if (station.getTrainCapacity() != null && station.getTrainCapacity() > 0) {
                hasCapacity = true;
                break;
            }
        }

        if (!hasCapacity) {
            return new ActionResult(false, ACTION_FAILED_NO_TRAIN_CAPACITY_LEFT);
        }

        // create the train via PlayComponentsStore (it will deduct resources)
        ActionResult result = playComponentsStore.buyTrain(playerOptional.get());
        if (!result.isSuccessful()) {
            return result;
        }

        // Find the newly created train by uid from result
        String newTrainUid = result.getUid();
        Optional<Train> newTrainOpt = playerOptional.get().getTrains().stream()
                .filter(t -> t.getUId().equals(newTrainUid)).findFirst();

        if (newTrainOpt.isEmpty()) {
            return new ActionResult(true, "Train bought but assignment failed (train not found)", newTrainUid);
        }

        Train newTrain = newTrainOpt.get();

        // Determine best station: maximize remaining customer potential = location.customers - usedCapacity
        Station bestStation = null;
        int bestRemaining = Integer.MIN_VALUE;

        Map map = MapService.getMap();

        for (Station station : playerOptional.get().getStations()) {
            if (station.getTrainCapacity() == null || station.getTrainCapacity() <= 0)
                continue;

            // find location associated with this station on the map
            Integer customers = null;
            if (map != null && map.getMap() != null) {
                for (var row : map.getMap()) {
                    for (var field : row) {
                        var loc = field.getLocation();
                        if (loc != null && loc.getStation() != null && loc.getStation().getUId() != null
                                && loc.getStation().getUId().equals(station.getUId())) {
                            customers = loc.getCustomers();
                            break;
                        }
                    }
                    if (customers != null)
                        break;
                }
            }

            // if no location or customers found, treat customers as 0
            int cust = (customers == null) ? 0 : customers;

            int usedCapacity = 0;
            if (station.getTrains() != null) {
                for (Train t : station.getTrains()) {
                    if (t != null && t.getCapacity() != null)
                        usedCapacity += t.getCapacity();
                }
            }

            int remaining = cust - usedCapacity;

            if (remaining > bestRemaining) {
                bestRemaining = remaining;
                bestStation = station;
            }
        }

        // If no station with location/customers found or all equal/exceed threshold, pick any station with capacity
        if (bestStation == null) {
            for (Station station : playerOptional.get().getStations()) {
                if (station.getTrainCapacity() != null && station.getTrainCapacity() > 0) {
                    bestStation = station;
                    break;
                }
            }
        }

        // Assign train to station
        if (bestStation != null) {
            newTrain.setStationUid(bestStation.getUId());
            if (bestStation.getTrains() == null) {
                bestStation.setTrains(new java.util.ArrayList<>());
            }
            bestStation.getTrains().add(newTrain);
            // decrement available train capacity
            bestStation.setTrainCapacity(bestStation.getTrainCapacity() - 1);
        }

    return result;
    }

    public ActionResult upgradeTrain(String sessionName, String playerUid, String trainUid) {
        SessionData sessionData = findSessionByName(sessionName);
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);

        if (playerOptional.isEmpty()) {
            return new ActionResult(false, ACTION_FAILED_NO_MATCH_PLAYER);
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        return playComponentsStore.upgradeTrain(playerOptional.get(), trainUid);
    }

    public List<UpgradeRequirements> getTrainUpgradeRequirements(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }
        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return null;
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        // Delegate to ResourceValidator to build upgrade requirements for all objects
        // then filter trains
        List<UpgradeRequirements> all = resourceValidator.getUpgradeRequirements(playerOptional.get());
        if (all == null || all.isEmpty()) {
            return null;
        }

        List<UpgradeRequirements> trainReqs = new ArrayList<>();
        for (UpgradeRequirements req : all) {
            boolean isTrain = playerOptional.get().getTrains().stream()
                    .anyMatch(t -> t.getUId().equals(req.getUIdOfObjectToUpgrade()));
            if (isTrain)
                trainReqs.add(req);
        }

        return trainReqs.isEmpty() ? null : trainReqs;
    }

    public List<UpgradeRequirements> getStationUpgradeRequirements(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return null;
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        List<UpgradeRequirements> all = resourceValidator.getUpgradeRequirements(playerOptional.get());
        if (all == null || all.isEmpty()) {
            return null;
        }

        List<UpgradeRequirements> stationReqs = new ArrayList<>();
        for (UpgradeRequirements req : all) {
            boolean isStation = playerOptional.get().getStations().stream()
                    .anyMatch(s -> s.getUId().equals(req.getUIdOfObjectToUpgrade()));
            if (isStation)
                stationReqs.add(req);
        }

        return stationReqs.isEmpty() ? null : stationReqs;
    }

    public UpgradeRequirements getStationUpgradeRequirements(String sessionName, String playerUid, String stationUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return null;
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        List<UpgradeRequirements> all = resourceValidator.getUpgradeRequirements(playerOptional.get());
        if (all == null || all.isEmpty())
            return null;

        for (UpgradeRequirements req : all) {
            if (req.getUIdOfObjectToUpgrade().equals(stationUid))
                return req;
        }

        return null;
    }

    public List<UpgradeRequirements> getRailUpgradeRequirements(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return null;
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        List<UpgradeRequirements> all = resourceValidator.getUpgradeRequirements(playerOptional.get());
        if (all == null || all.isEmpty()) {
            return null;
        }

        List<UpgradeRequirements> railReqs = new ArrayList<>();
        for (UpgradeRequirements req : all) {
            boolean isRail = playerOptional.get().getRails().stream()
                    .anyMatch(r -> r.getUId().equals(req.getUIdOfObjectToUpgrade()));
            if (isRail)
                railReqs.add(req);
        }

        return railReqs.isEmpty() ? null : railReqs;
    }

    public UpgradeRequirements getRailUpgradeRequirements(String sessionName, String playerUid, String railUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null) {
            return null;
        }

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty()) {
            return null;
        }

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        List<UpgradeRequirements> all = resourceValidator.getUpgradeRequirements(playerOptional.get());
        if (all == null || all.isEmpty())
            return null;

        for (UpgradeRequirements req : all) {
            if (req.getUIdOfObjectToUpgrade().equals(railUid))
                return req;
        }

        return null;
    }

    public List<UpgradeRequirements> getBuyRequirements(String sessionName, String playerUid) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null)
            return null;

        Optional<Player> playerOptional = PlayerUtil.getPlayerByUid(sessionData.getSessionPlayers(), playerUid);
        if (playerOptional.isEmpty())
            return null;

        resourceCalculator.calculateResource(List.of(playerOptional.get()), sessionData.getSessionClock());

        return resourceValidator.getBuyRequirements(playerOptional.get());
    }

    public List<Player> getRanking(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData.getGameState().equals(NOT_CREATED) || sessionData.getGameState().equals(NOT_STARTED)) {
            throw new IllegalStateException("get ranking failed - session is not created or not started");
        }

        List<Player> playerDtos = new ArrayList<>(sessionData.getSessionPlayers());
        List<Player> sortedPlayers = new ArrayList<>();

        for (Player p : playerDtos) {

            for (Station s : p.getStations()) {
                p.setPoints(p.getPoints() + (3 + (2 * s.getLevel())));
            }

            for (Train t : p.getTrains()) {
                p.setPoints(p.getPoints() + (int) (2 + (1.5 * t.getLevel())));
            }

            for (Rail r : p.getRails()) {
                p.setPoints(p.getPoints() + (int) (1 + r.getLevel()));
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


    public Map getMap(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || sessionData.getGameState().equals(NOT_CREATED)
                || sessionData.getGameState().equals(NOT_STARTED)) {
            throw new IllegalStateException("get map failed - session is not created or not started");
        }

        // Ensure map locations have been generated. If no Field contains a Location yet,
        // generate borders/locations once.
        Map map = MapService.getMap();
        boolean hasAnyLocation = false;
        if (map != null && map.getMap() != null) {
            for (var row : map.getMap()) {
                for (var field : row) {
                    if (field.getLocation() != null) {
                        hasAnyLocation = true;
                        break;
                    }
                }
                if (hasAnyLocation) break;
            }
        }

        if (!hasAnyLocation) {
            MapService.generateBordersForPlayers();
        }

        return map;
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

        // Fix: Add null checks for sessionClock
        if (sessionData.getSessionClock() != null) {
            dto.setSessionStarted(sessionData.getSessionClock().getStarted());
            dto.setSessionEnded(sessionData.getSessionClock().getEnded());
        } else {
            dto.setSessionStarted(null);
            dto.setSessionEnded(null);
        }

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

    public PlayerOverviewDto createPlayerOverview(String uid, String name) {
        PlayerOverviewDto dto = new PlayerOverviewDto();
        dto.setUId(uid);
        dto.setName(name);
        return dto;
    }

    public long getRuntime(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || sessionData.getSessionClock() == null) {
            return 0;
        }

        return sessionData.getSessionClock().getSessionDurationInSeconds();
    }

    public ActionResult triggerRandomEvent(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData == null || sessionData.getGameState() != GameStateEnum.RUNNING) {
            return new ActionResult(false, "Session is not running");
        }

        long eventInterval = 60 * 5; // 5 minutes
        if (sessionData.getSessionClock().getEventClock() != null &&
                java.time.Duration.between(sessionData.getSessionClock().getEventClock(), OffsetDateTime.now())
                        .toSeconds() < eventInterval) {
            return new ActionResult(false, "Random event already triggered recently");
        }

        sessionData.getSessionClock().setEventClock(OffsetDateTime.now());

        return eventService.triggerRandomEvent(sessionData);
    }

    public void checkExpiredEvents(String sessionName) {
        SessionData sessionData = findSessionByName(sessionName);
        if (sessionData != null) {
            eventService.checkAndReverseExpiredEvents(sessionData);
        }

    }
}
