package de.gts.redrail.game.constants;

public interface ResponseText {
    //Session
    String CREATED_SESSION = "session successfully created";
    String CREATE_SESSION_FAILED_SESSION_IS_ALREADY_CREATED = "session creation failed - session is already created";
    String STARTED_SESSION = "session started";
    String END_SESSION = "session ended";
    String START_SESSION_FAILED_NO_PLAYER = "session start failed - no player joined session";
    String START_SESSION_FAILED_SESSION_IS_NOT_CREATED_IS_RUNNING_OR_FINISHED = "session start failed - session has wrong status";

    //Join Session
    String PLAYER_JOINED_SESSION = "player successfully joined session";
    String PLAYER_JOIN_SESSION_FAILED_SESSION_IS_RUNNING_OR_FINISHED = "player joining session failed - session is running or finished";
    String PLAYER_JOIN_SESSION_FAILED = "failed to joining player to session - maybe already joined session";

    //Action
    String CANT_AFFORD_NEW_PLAY_COMPONENT = "buy new play component failed - can't afford new play component";
    String REQIUERMENT_NOT_MET = "action failed - requirements for buying train are not met";
    String CANT_AFFORD_UPGRADE_PLAY_COMPONENT = "upgrade play component failed - can't afford upgrade";
    String BOUGHT_NEW_PLAY_COMPONENT = "successfully bought new play component";
    String BOUGHT_UPGRADE = "successfully bought upgrade";
    String ACTION_FAILED_NO_MATCH_PLAYER = "action failed - no player matched uid";
    String ACTION_FAILED_NO_MATCH_PLAY_COMPONENT = "action failed - no play component matched uid";
    String GET_PLAYERS_FAILED_SESSION_IS_NOT_RUNNING = "get players failed - session is not running";
    String GET_RESOURCE_FAILED_SESSION_IS_NOT_RUNNING = "get resource failed - session is not running";
    String GET_RESOURCE_FAILED_NO_MATCH_PLAYER = "get resource failed - no player matched uid";
    String GET_RESOURCE_FAILED_NO_MATCH_PLAY_COMPONENT = "get resource failed - no play component matched uid";
    String ACTION_FAILED_NO_TRAIN_CAPACITY_LEFT = "action failed - Your stations have no train capacity left";
    String ACTION_FAILED_MAX_LEVEL_REACHED = "action failed - max level reached for this play component";
    String NOT_ENOUGH_EMPLOYEES = "action failed - not enough employees available for this action";
}
