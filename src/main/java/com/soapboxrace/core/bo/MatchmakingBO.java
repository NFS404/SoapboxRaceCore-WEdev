/*
 * Taken from SBRW WorldUnited.gg, original code by HeyItsLeo
 */

package com.soapboxrace.core.bo;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;

import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

/**
 * Responsible for managing the multiplayer matchmaking system.
 * This deals with 2 classes of events: restricted and open.
 * When asked for a persona for a given car class, the matchmaker
 * will check if that class is open or restricted. Open events will receive
 * players of any class, while restricted events will only receive players of
 * the required class.
 *
 * @author heyitsleo, Hypercycle
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MatchmakingBO {

    @EJB
    private RedisBO redisBO;

    @EJB
    private ParameterBO parameterBO;
    
    @EJB
    private LobbyDAO lobbyDAO;
    
    @EJB
    private EventResultBO eventResultBO;
    
    @EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

    private StatefulRedisConnection<String, String> redisConnection;

    @PostConstruct
    public void initialize() {
        if (parameterBO.getBoolParam("REDIS_ENABLE")) {
            this.redisConnection = this.redisBO.getConnection();
            this.redisConnection.sync().del("matchmaking_queue"); // Delete any of MM queue entrants, if any exists
            System.out.println("Initialized matchmaking system");
        } else {
        	System.out.println("Redis is not enabled! Matchmaking queue is disabled.");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (this.redisConnection != null) {
            this.redisConnection.sync().del("matchmaking_queue");
        }
        System.out.println("Shutdown matchmaking system");
    }

    /**
     * Adds the given Persona ID to the queue under the given search priorities.
     * CarClass, RaceFilter and isAvailable is saved as "PlayerVehicleInfo" string (Value), since personaId is a Key.
     *
     * @param personaId The ID of the persona to add to the queue.
     * @param carClass The class of the persona's current car.
     * @param raceFilter Race Filter value
     * @param isAvailable Is that entrant is available to be invited to races (1 - true, 0 - false)
     * @param searchStage Player lobby search stage (1 - class-restricted races only, 2 - priority class groups search, 3 - search all open lobbies)
     */
    public void addPlayerToQueue(Long personaId, Integer carClass, Integer raceFilter, int isAvailable, int searchStage) {
    	String playerVehicleInfo = carClass.toString() + "," + raceFilter.toString() + "," + isAvailable + "," + searchStage;
        if (this.redisConnection != null) {
            this.redisConnection.sync().hset("matchmaking_queue", personaId.toString(), playerVehicleInfo);
            matchmakingWebStatus();
//          System.out.println("playerCount add (+1): " + curPlayerCount);
        }
    }

    /**
     * Removes the given persona ID from the queue.
     *
     * @param personaId The ID of the persona to remove from the queue.
     */
    public void removePlayerFromQueue(Long personaId) {
        if (this.redisConnection != null) {
            this.redisConnection.sync().hdel("matchmaking_queue", personaId.toString());
            matchmakingWebStatus();
//          System.out.println("playerCount remove (+1): " + curPlayerCount);
        }
    }

    /**
     * Gets the ID of a persona from the queue, as long as that persona is listed under the given car class.
     * Basically, we are "pulling" the racers from MM queue to our event, if they able to participate.
     * playerVehicleInfo array: slot 0 - Car Class, slot 1 - Race Filter value, slot 2 - isAvailable value
     *
     * @param carClass The car class hash to find a persona in.
     * @param eventModeId Event Mode ID
     * @param hosterCarClass Car class hash of first player's car
     * @return The ID of the persona, or {@literal -1} if no persona was found.
     */
    public Long getPlayerFromQueue(Integer carClass, int eventModeId, int hosterCarClass, boolean isSClassFilterActive) {
        if (this.redisConnection == null) {
        	System.out.println("### redisConnection FUCKED");
            return -1L;
        }

        ScanIterator<KeyValue<String, String>> searchIterator = ScanIterator.hscan(this.redisConnection.sync(), "matchmaking_queue");
        System.out.println("### getPlayerFromQueue");
        long personaId = -1L;

        while (searchIterator.hasNext()) {
            KeyValue<String, String> keyValue = searchIterator.next();
            String[] playerVehicleInfo = keyValue.getValue().split(",");
            System.out.println("### getPlayerFromQueue 2");
            
            // Open event, or when player have a valid car class for restricted event
            int playerCarClass = Integer.parseInt(playerVehicleInfo[0]);
            int playerRaceFilter = Integer.parseInt(playerVehicleInfo[1]);
            int searchStage = Integer.parseInt(playerVehicleInfo[3]);
            if (Integer.parseInt(playerVehicleInfo[2]) == 1 && isRaceFilterAllowed(playerRaceFilter, eventModeId) 
            		&& isSClassFilterAllowed(playerCarClass, hosterCarClass, carClass, isSClassFilterActive) 
            		&& isPriorityClassFilterAllowed(playerCarClass, carClass, hosterCarClass, searchStage)) {
                personaId = Long.parseLong(keyValue.getKey());
                String newPlayerVehicleInfo = playerCarClass + "," + playerRaceFilter + "," + 0; // This entrant is not available for new invites
                this.redisConnection.sync().hset("matchmaking_queue", keyValue.getKey(), newPlayerVehicleInfo);
                // removePlayerFromQueue(personaId);
                System.out.println("### getPlayerFromQueue FINAL");
                break;
            }
        }
        return personaId;
    }
    
    @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void matchmakingWebStatus() {
    	Long test = this.redisConnection.sync().hlen("matchmaking_queue"); // Get the count of Key-Value pairs (player entries) by "queue" hash
        System.out.println("### Players searching on Race Now: " + test);
        List<LobbyEntity> lobbiesList = lobbyDAO.findAllOpen();
        if (lobbiesList.isEmpty()) {
        	System.out.println("### No public lobbies is available yet.");
        }
        else {
        	for (LobbyEntity lobby : lobbiesList) {
        		int lobbyHosterCarClass = lobby.getCarClassHash();
        		EventEntity lobbyEvent = lobby.getEvent();
        		String isTimerActive = "Search";
        		if (lobby.getLobbyDateTimeStart() != null) {
        			isTimerActive = "Waiting";
        		}
        		String eventName = lobbyEvent.getName();
        		int eventMode = lobbyEvent.getEventModeId();
        		int eventCarClass = lobbyEvent.getCarClassHash();
        		Long lobbyTeamPlayer = lobby.getTeam1Id();
        		
        		String lobbyHosterCarClassStr = eventResultBO.getCarClassLetter(lobbyHosterCarClass);
        		String eventCarClassStr = eventResultBO.getCarClassLetter(eventCarClass);
        		
        		StringBuilder lobbyInfoOutput = new StringBuilder();
        		lobbyInfoOutput.append("### Mode: " + eventMode + ", ");
        		lobbyInfoOutput.append("Class: " + eventCarClassStr + ", ");
        		lobbyInfoOutput.append("Event: " + eventName + ", ");
        		lobbyInfoOutput.append("Priority Class: " + lobbyHosterCarClassStr + ", ");
        		if (lobbyTeamPlayer != null) {
        			lobbyInfoOutput.append("[T], ");
        		}
        		lobbyInfoOutput.append("Status: " + isTimerActive + ", ");
        		System.out.println(lobbyInfoOutput.toString());
            }
        }
    }
    
    /**
     * Checks if player Race Filter is allowing him to participate on the suggested event.
     *
     * @param playerRaceFilter Race Filter value (mode)
     * @param eventModeId Game mode ID of the event
     * @return Is that event allowed by Race Filter (true/false)
     */
    public boolean isRaceFilterAllowed(int playerRaceFilter, int eventModeId) {
    	boolean isRaceFilterAllowed = false;
    	switch (playerRaceFilter) {
    	case 1: // Sprint & Circuit
			if (eventModeId == 4 || eventModeId == 9) {isRaceFilterAllowed = true;}
			break;
		case 2: // Drag
			if (eventModeId == 19) {isRaceFilterAllowed = true;}
			break;	
		case 3: // All Races
			if (eventModeId == 4 || eventModeId == 9 || eventModeId == 19) {isRaceFilterAllowed = true;}
			break;
		case 4: // Team Escape
			if (eventModeId == 24 || eventModeId == 100) {isRaceFilterAllowed = true;}
			break;
		default: // No Filter
			isRaceFilterAllowed = true;
			break;
    	}
        System.out.println("### isRaceFilterAllowed: " + isRaceFilterAllowed);
        return isRaceFilterAllowed;
    }
    
    /**
     * Checks if player car class fits with his search stage (class group priority) and the event class-restriction.
     *
     * @param playerCarClass Car class hash of player car
     * @param eventCarClass Class-restriction of the event
     * @param hosterCarClass Car class hash of first player's car
     * @param Player lobby search stage (1 - class-restricted races only, 2 - priority class groups search, 3 - search all open lobbies)
     * @return Is that event allowed by Priority Class Filter (true/false)
     */
    public boolean isPriorityClassFilterAllowed(int playerCarClass, int eventCarClass, int hosterCarClass, int searchStage) {
    	boolean isPriorityClassFilterAllowed = false;
    	if (eventCarClass == 607077938) {
    		if (searchStage == 2) { // If race search is on stage 2, player should get the lobbies which fits his class priority group
        		switch (hosterCarClass) {
        		case -2142411446: // S Class group
        			if (playerCarClass == -2142411446) {
        				isPriorityClassFilterAllowed = true;
        			}
        			break;
        		case -405837480:
    			case -406473455: // A-B Classes group
    				if (playerCarClass == -405837480 || playerCarClass == -406473455) {
        				isPriorityClassFilterAllowed = true;
        			}
    				break;
    			case 1866825865:
    			case 415909161:
    			case 872416321: // C-D-E Classes group
    				if (playerCarClass == 1866825865 || playerCarClass == 415909161 || playerCarClass == 872416321) {
        				isPriorityClassFilterAllowed = true;
        			}
    				break;
        		}
        	}
        	if (searchStage == 3) { // Filter is inactive, search for any open lobby
        		isPriorityClassFilterAllowed = true;
        	}
    	}
    	else if (playerCarClass == eventCarClass) { // Class-restricted race and player fits in
        		isPriorityClassFilterAllowed = true;
        }
        System.out.println("### isPriorityClassFilterAllowed: " + isPriorityClassFilterAllowed + ", searchStage: " + searchStage);
        return isPriorityClassFilterAllowed;
    }
    
    /**
     * Checks if player is able to participate on event, where the hoster is on S-class car.
     * Needs RACENOW_SCLASS_SEPARATE parameter to be active, otherwise it's always returns true.
     *
     * @param playerCarClass player car class
     * @param hosterCarClass first player (hoster) car class
     * @param eventCarClass event car class restriction
     * @return Is that player is allowed to participate by S-Class Filter (true/false)
     */
    public boolean isSClassFilterAllowed(int playerCarClass, int hosterCarClass, int eventCarClass, boolean isSClassFilterActive) {
    	int SClassHash = -2142411446;
    	boolean isSClassFilterAllowed = true;
    	// We don't need to check S-class restricted races
    	if (eventCarClass != SClassHash && isSClassFilterActive && (hosterCarClass == SClassHash || SClassHash == playerCarClass) 
    			&& playerCarClass != hosterCarClass) {
    		isSClassFilterAllowed = false; // Only S-Class cars is able to participate on races, which is hosted by players on S-class cars 
    	}
        System.out.println("### isSClassFilterAllowed: " + isSClassFilterAllowed);
        return isSClassFilterAllowed;
    }

    /**
     * Add the given event ID to the list of ignored events for the given persona ID.
     *
     * @param personaId the persona ID
     * @param eventId the event ID
     */
    public void ignoreEvent(long personaId, EventEntity EventEntity) {
    	// Event will be ignored only when player is on Race Now search
        if (this.redisConnection != null) {
        	System.out.println("### hexists: " + this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId)));
        	if (this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId))) {
        		this.redisConnection.sync().sadd("ignored_events." + personaId, Long.toString(EventEntity.getId()));
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + EventEntity.getName() + " will be ignored in the Race Now search for a while."), personaId);
        	}
        }
    }
    
    /**
     * Checks if the specified player is on Race Now search.
     *
     * @param personaId the persona ID
     */
    public boolean isPlayerOnMMSearch(long personaId) {
    	boolean isExists = false;
        if (this.redisConnection != null) {
        	isExists = this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId));
        }
        return isExists;
    }

    /**
     * Resets the list of ignored events for the given persona ID
     *
     * @param personaId the persona ID
     */
    public void resetIgnoredEvents(long personaId) {
        if (this.redisConnection != null) {
            this.redisConnection.sync().del("ignored_events." + personaId);
        }
    }

    /**
     * Checks if the given event ID is in the list of ignored events for the given persona ID
     *
     * @param personaId the persona ID
     * @param eventId the event ID
     * @return {@code true} if the given event ID is in the list of ignored events for the given persona ID
     */
    public boolean isEventIgnored(long personaId, long eventId) {
        if (this.redisConnection != null) {
            return this.redisConnection.sync().sismember("ignored_events." + personaId, Long.toString(eventId));
        }
        return false;
    }
    
    /**
     * Get the ignored by persona events list
     *
     * @param personaId the persona ID
     * @return int-list of ignored eventId
     */
    public List<Integer> getEventIgnoredList(long personaId) {
    	List<Integer> eventIgnoreList = new ArrayList<Integer>();
        if (this.redisConnection != null) {
            ScanIterator<String> ignoreIterator = ScanIterator.sscan(this.redisConnection.sync(), "ignored_events." + personaId);
            while (ignoreIterator.hasNext()) {
            	String eventId = ignoreIterator.next();
            	eventIgnoreList.add(Integer.parseInt(eventId));
            }
        }
        return eventIgnoreList;
    }

    @Asynchronous
    @Lock(LockType.READ)
    public void handlePersonaPresenceUpdated(PersonaPresenceEntity personaPresenceEntity) {
        removePlayerFromQueue(personaPresenceEntity.getActivePersonaId());
    }
}