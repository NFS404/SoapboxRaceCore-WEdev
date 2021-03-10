/*
 * Taken from SBRW WorldUnited.gg, original code by HeyItsLeo
 */

package com.soapboxrace.core.bo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class RedisBO {

    @EJB
    private ParameterBO parameterBO;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisURI redisURI;

    @PostConstruct
    public void init() {
        if (this.parameterBO.getBoolParam("REDIS_ENABLE")) {
            String redisHost = parameterBO.getStrParam("REDIS_HOST");
            Integer redisPort = parameterBO.getIntParam("REDIS_PORT");
            String redisPassword = parameterBO.getStrParam("REDIS_PASSWORD");

            this.redisURI = RedisURI.builder().withHost(redisHost).withPort(redisPort).withPassword(redisPassword.toCharArray()).build();
            this.redisClient = RedisClient.create();

            try {
                this.connection = this.redisClient.connect(redisURI);
                System.out.println("Connected to Redis server at "+ redisHost+ ":" + redisPort + "");
            } catch (RedisException exception) {
                throw new RuntimeException("Failed to connect to Redis server at " + redisHost + ":" + redisPort, exception);
            }
        }
    }

    public StatefulRedisPubSubConnection<String, String> createPubSub() {
        if (this.redisClient == null) {
            throw new RuntimeException("Redis is disabled!");
        }
        return this.redisClient.connectPubSub(redisURI);
    }

    public RedisClient getRedisClient() {
        if (this.redisClient == null) {
            throw new RuntimeException("Redis is disabled!");
        }
        return redisClient;
    }

    public StatefulRedisConnection<String, String> getConnection() {
        if (this.redisClient == null) {
            throw new RuntimeException("Redis is disabled!");
        }
        return connection;
    }
}