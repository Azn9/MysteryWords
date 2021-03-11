package dev.azn9.mysteryWords.services;

import dev.azn9.mysteryWords.config.Configuration;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import reactor.core.publisher.Mono;

public class CacheService {

    private final DatabaseService        databaseService;
    private final RedissonReactiveClient redisClient;

    public CacheService(RedissonReactiveClient redisClient, DatabaseService databaseService) {
        this.redisClient = redisClient;
        this.databaseService = databaseService;
    }

    public Mono<Configuration> getGuildConfiguration(Long guildId) {
        RMapReactive<Long, Configuration> map = redisClient.getMap("configurations");

        return map.get(guildId).flatMap(configuration -> {
            if (configuration == null)
                return databaseService.getGuildConfiguration(guildId).flatMap(configuration1 -> {
                    if (configuration1 == null) {
                        Configuration configuration2 = new Configuration(guildId);

                        return map.fastPut(guildId, configuration2).flatMap(aBoolean -> {
                            if (!aBoolean)
                                System.err.println("Can't cache data for guild " + guildId + " !");

                            return Mono.just(configuration2);
                        });
                    }

                    return Mono.just(configuration1);
                });
            else
                return Mono.just(configuration);
        });
    }

}
