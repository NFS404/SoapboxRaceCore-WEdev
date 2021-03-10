/*
 * Taken from SBRW WorldUnited.gg, original code by HeyItsLeo
 */

package com.soapboxrace.core.bo;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;

import com.soapboxrace.core.jpa.PersonaPresenceEntity;

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

    private StatefulRedisConnection<String, String> redisConnection;

    @PostConstruct
    public void initialize() {
        if (parameterBO.getBoolParam("REDIS_ENABLE")) {
            this.redisConnection = this.redisBO.getConnection();
            this.redisConnection.sync().hset("matchmaking_info", "playerCount", "0"); // Create the player count entry
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
     * CarClass and RaceFilter is saved as "PlayerVehicleInfo" string (Value), since personaId is a Key.
     *
     * @param personaId The ID of the persona to add to the queue.
     * @param carClass The class of the persona's current car.
     */
    public void addPlayerToQueue(Long personaId, Integer carClass, Integer raceFilter) {
    	String playerVehicleInfo = carClass.toString() + "," + raceFilter.toString();
        if (this.redisConnection != null) {
            this.redisConnection.sync().hset("matchmaking_queue", personaId.toString(), playerVehicleInfo);
            //int playerCountOld = Integer.getInteger(this.redisConnection.sync().hget("matchmaking_info", "playerCount")); // Get the queue player count
            //this.redisConnection.sync().hset("matchmaking_info", "playerCount", String.valueOf(playerCountOld++)); // Save the new queue player count
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
            //int playerCountOld = Integer.getInteger(this.redisConnection.sync().hget("matchmaking_info", "playerCount")); // Get the queue player count
            //this.redisConnection.sync().hset("matchmaking_info", "playerCount", String.valueOf(playerCountOld--)); // Save the new queue player count
        }
    }

    /**
     * Gets the ID of a persona from the queue, as long as that persona is listed under the given car class.
     * Basically, we are "pulling" the racers from MM queue to our event, if they able to participate.
     * playerVehicleInfo: 0 - Car Class, 1 - Race Filter value
     *
     * @param carClass The car class hash to find a persona in.
     * @return The ID of the persona, or {@literal -1} if no persona was found.
     */
    public Long getPlayerFromQueue(Integer carClass) {
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
            // FIXME RaceFilter should filter stuff
            if (carClass == 607077938 || Integer.parseInt(playerVehicleInfo[0]) == carClass) {
                personaId = Long.parseLong(keyValue.getKey());
                removePlayerFromQueue(personaId);
                System.out.println("### getPlayerFromQueue FINAL");
                break;
            }
        }
        return personaId;
    }
    
    // @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void matchmakingWebStatus() {
    	String test = this.redisConnection.sync().hget("matchmaking_info", "playerCount");
        System.out.println("### Players searching: " + test);
    }

    /**
     * Add the given event ID to the list of ignored events for the given persona ID.
     *
     * @param personaId the persona ID
     * @param eventId   the event ID
     */
    public void ignoreEvent(long personaId, long eventId) {
        if (this.redisConnection != null) {
            this.redisConnection.sync().sadd("ignored_events." + personaId, Long.toString(eventId));
        }
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
     * @param eventId   the event ID
     * @return {@code true} if the given event ID is in the list of ignored events for the given persona ID
     */
    public boolean isEventIgnored(long personaId, long eventId) {
        if (this.redisConnection != null) {
            return this.redisConnection.sync().sismember("ignored_events." + personaId, Long.toString(eventId));
        }

        return false;
    }

    @Asynchronous
    @Lock(LockType.READ)
    public void handlePersonaPresenceUpdated(PersonaPresenceEntity personaPresenceEntity) {
        removePlayerFromQueue(personaPresenceEntity.getActivePersonaId());
    }
}