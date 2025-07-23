package de.gts.redrail.game.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import de.gts.redrail.game.component.GameClock;
import de.gts.redrail.game.constants.GameStateEnum;
import static de.gts.redrail.game.constants.ResponseText.STARTED_SESSION;
import static de.gts.redrail.game.constants.ResponseText.START_SESSION_FAILED_NO_PLAYER;
import static de.gts.redrail.game.constants.ResponseText.START_SESSION_FAILED_SESSION_IS_NOT_CREATED_IS_RUNNING_OR_FINISHED;
import de.gts.redrail.game.models.dtos.PlayerDto;
import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import de.gts.redrail.game.models.dtos.SessionEndResponseDto;
import de.gts.redrail.game.models.dtos.SessionOverviewDto;
import de.gts.redrail.game.models.entities.ActionResult;
import de.gts.redrail.game.models.entities.Player;
import de.gts.redrail.game.service.SessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SessionController {

    public final SessionService sessionService;
    public final GameClock gameClock;

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionOverviewDto>> getSessionOverview() {
        return ResponseEntity.ok(sessionService.getAllSessionsOverview());
    }

    @GetMapping("/session/{sessionName}/info")
    public ResponseEntity<SessionOverviewDto> getSessionInfo(@PathVariable(name = "sessionName") String sessionName) {
        return ResponseEntity.ok(sessionService.createSessionOverview(sessionName));
    }

    @GetMapping("/session/{sessionName}/player/{playerUid}/resource")
    public ResponseEntity<PlayerDto> getResource(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid")  String playerUid) {
        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().body(null);
        }

        PlayerDto playerDto = sessionService.getPlayerStatus(playerUid, sessionName);

        if (playerDto != null) {
            return ResponseEntity.ok(playerDto);
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/session/{sessionName}")
    public ResponseEntity<SessionOverviewDto> createSession(@PathVariable(name = "sessionName")  String sessionName) {
       

        return ResponseEntity.ok(sessionService.createSession(sessionName));
    }

    @PatchMapping("/session/{sessionName}/start")
    public ResponseEntity<String> startSession(@PathVariable String sessionName) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.NOT_STARTED)) {
            return ResponseEntity.badRequest().body(START_SESSION_FAILED_SESSION_IS_NOT_CREATED_IS_RUNNING_OR_FINISHED);
        }

        boolean result = sessionService.startSession(sessionName);

        if (result) {
            return ResponseEntity.ok(STARTED_SESSION);
        } else {
            return ResponseEntity.badRequest().body(START_SESSION_FAILED_NO_PLAYER);
        }
    }

    @PatchMapping("/session/{sessionName}/end")
    public ResponseEntity<SessionEndResponseDto> endSession(@PathVariable String sessionName) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().body(null);
        }

        List<PlayerDto> players = sessionService.getAllPlayer(sessionName);
        long duration = sessionService.endSession(sessionName);
        return ResponseEntity.ok(new SessionEndResponseDto(players, duration));
    }

    @PatchMapping("/session/{sessionName}/kill")
    public ResponseEntity<String> killSession(@PathVariable(name = "sessionName")  String sessionName) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        sessionService.killSession(sessionName);

        return ResponseEntity.ok("Session killed successfully");
    }

    @PatchMapping("/sessions/killall")
    public ResponseEntity<String> killAllSessions() {
        sessionService.killAllSessions();
        return ResponseEntity.ok("All sessions killed successfully");
    }

    @PostMapping("/session/{sessionName}/{playerName}")
    public ResponseEntity<SessionOverviewDto> joinSession(
            @PathVariable String sessionName,
            @PathVariable String playerName
            ) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.ok(sessionService.createSessionOverview(sessionName));
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.NOT_STARTED)) {
            return ResponseEntity.ok(sessionService.createSessionOverview(sessionName));
        }

        PlayerOverviewDto playerOverviewDto = new PlayerOverviewDto();
        playerOverviewDto.setUId(UUID.randomUUID().toString());
        playerOverviewDto.setName(playerName);
        boolean result = sessionService.joinSession(playerOverviewDto, sessionName);

        if (!result) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(sessionService.createSessionOverview(sessionName));
    }

    @GetMapping("/session/{sessionName}/GetPlayers")
    public ResponseEntity<List<PlayerOverviewDto>> getPlayers(@PathVariable(name = "sessionName") String sessionName) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        List<PlayerOverviewDto> players = sessionService.getAllPlayerOverview(sessionName);

        if (players != null && !players.isEmpty()) {
            return ResponseEntity.ok(players);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/session/{sessionName}/player/{playerUid}")
    public ResponseEntity<PlayerDto> getPlayerStatus(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.noContent().build();
        }

        PlayerDto playerDto = sessionService.getPlayerStatus(playerUid, sessionName);

        if (playerDto != null) {
            return ResponseEntity.ok(playerDto);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/session/{sessionName}/player/{playerUid}/rail")
    public ResponseEntity<String> buyRail(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        ActionResult actionResult = sessionService.buyRail(playerUid, sessionName);
        
        if (actionResult.isSuccessful()) {
            return ResponseEntity.ok(actionResult.getUid());
        } else {
            return ResponseEntity.badRequest().body(actionResult.getMessage());
        }
    }

    @PatchMapping("/session/{sessionName}/player/{playerUid}/rail/{railUid}/upgrade")
    public ResponseEntity<String> upgradeRail(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid, @PathVariable(name = "railUid") String railUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }
        
        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        ActionResult actionResult = sessionService.upgradeRail(sessionName, playerUid, railUid);
        return handleActionResult(actionResult);
    }

    @PatchMapping("/session/{sessionName}/player/{playerUid}/train/{trainUid}/upgrade")
    public ResponseEntity<String> upgradeTrain(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid, @PathVariable(name = "trainUid") String trainUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (trainUid == null || trainUid.isEmpty()) {
            return ResponseEntity.badRequest().body("Train UID must not be null or empty");
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        ActionResult actionResult = sessionService.upgradeTrain(playerUid, trainUid, sessionName);
        return handleActionResult(actionResult);
    }

    @PostMapping("/session/{sessionName}/player/{playerUid}/train")
    public ResponseEntity<String> buyTrain(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        ActionResult actionResult = sessionService.buyTrain(playerUid,sessionName);

        if (actionResult.isSuccessful()) {
            return ResponseEntity.ok(actionResult.getUid());
        } else {
            return ResponseEntity.badRequest().body(actionResult.getMessage());
        }
    }

    @PostMapping("/session/{sessionName}/player/{playerUid}/station")
    public ResponseEntity<String> buyStation(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        ActionResult actionResult = sessionService.buyStation(playerUid, sessionName);

        if (actionResult.isSuccessful()) {
            return ResponseEntity.ok(actionResult.getUid());
        } else {
            return ResponseEntity.badRequest().body(actionResult.getMessage());
        }
    }

    @GetMapping("/session/{sessionName}/player/{playerUid}/station/{stationUid}")
    public ResponseEntity<String> upgradeStation(@PathVariable(name = "sessionName") String sessionName, @PathVariable(name = "playerUid") String playerUid, @PathVariable(name = "stationUid") String stationUid) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        if (stationUid == null || stationUid.isEmpty()) {
            return ResponseEntity.badRequest().body("Station UID must not be null or empty");
        }
        ActionResult actionResult = sessionService.upgradeStation(playerUid, stationUid, sessionName);
        return handleActionResult(actionResult);
    }

    private ResponseEntity<String> handleActionResult(ActionResult actionResult) {
        if (actionResult.isSuccessful()) {
            return ResponseEntity.ok(actionResult.getMessage());
        }
        
        else {
            return ResponseEntity.badRequest().body(actionResult.getMessage());
        }
    }

    @GetMapping("/session/{sessionName}/players/rainking")
    public ResponseEntity<List<Player>> rankingEntity(@PathVariable(name = "sessionName") String sessionName) {
        if (!sessionService.isSessionNameMatching(sessionName)) {
            return ResponseEntity.noContent().build();
        }

        if (!sessionService.getGameState(sessionName).equals(GameStateEnum.RUNNING)) {
            return ResponseEntity.badRequest().build();
        }

        List<Player> ranking = sessionService.getRanking(sessionName);
        return ResponseEntity.ok(ranking);
    }
    
}
