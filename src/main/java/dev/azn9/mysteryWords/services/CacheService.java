package dev.azn9.mysteryWords.services;

import dev.azn9.mysteryWords.config.Configuration;
import dev.azn9.mysteryWords.injector.Inject;
import org.redisson.RedissonReactive;
import org.redisson.api.RMapReactive;
import reactor.core.publisher.Mono;

public class CacheService {

    @Inject
    private static DatabaseService databaseService;

    @Inject
    private static RedissonReactive redisClient;

    public Mono<Configuration> getGuildConfiguration(Long guildId) {
        RMapReactive<Long, Configuration> map = redisClient.getMap("configurations");

        return map.containsKey(guildId).flatMap(aBoolean -> {
            if (aBoolean)
                return map.get(guildId).flatMap(configuration -> {
                    if (configuration == null)
                        return getFromDatabase(guildId);
                    else
                        return Mono.just(configuration);
                });
            else
                return getFromDatabase(guildId);
        });
    }

    private Mono<Configuration> getFromDatabase(Long guildId) {
        return databaseService.getGuildConfiguration(guildId).flatMap(configuration1 -> {
            if (configuration1 == null) {
                Configuration configuration2 = new Configuration(guildId);

                return redisClient.getMapCache("configurations").fastPut(guildId, configuration2).flatMap(aBoolean -> {
                    if (!aBoolean)
                        System.err.println("Can't cache data for guild " + guildId + " !");

                    return Mono.just(configuration2);
                });
            } else
                return Mono.just(configuration1);
        });
    }
}