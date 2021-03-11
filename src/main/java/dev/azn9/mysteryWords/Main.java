package dev.azn9.mysteryWords;

import dev.azn9.mysteryWords.services.CacheService;
import dev.azn9.mysteryWords.services.DatabaseService;
import dev.azn9.mysteryWords.services.EventService;
import discord4j.core.DiscordClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;

public class Main {

    private static CacheService cacheService;

    public static void main(String[] args) {
        try {
            String discordToken = System.getenv("DISCORD_TOKEN");
            String databaseHost = System.getenv("DATABASE_HOST");
            String databaseName = System.getenv("DATABASE_NAME");
            String databaseUser = System.getenv("DATABASE_USER");
            String databasePass = System.getenv("DATABASE_PASS");
            String redisUrl = System.getenv("REDIS_URL");

            assert discordToken != null : "You didn't provide the discord bot token !";
            assert databaseHost != null : "You didn't provide the database host url !";
            assert databaseName != null : "You didn't provide the database name !";
            assert databaseUser != null : "You didn't provide the database user !";
            assert databasePass != null : "You didn't provide the database password !";
            assert redisUrl != null : "You didn't provide the redis url !";

            DatabaseService databaseService = new DatabaseService(databaseHost, databaseName, databaseUser, databasePass);

            assert databaseService.getConnection() != null : "An error occurred while attempting to connect to the database !";

            Config config = new Config();
            config.useSingleServer().setAddress(redisUrl);
            RedissonReactiveClient redissonReactive = Redisson.createReactive(config);

            assert redissonReactive != null : "An error occurred while attempting to connect to the redis server !";

            cacheService = new CacheService(redissonReactive, databaseService);

            DiscordClient discordClient = DiscordClient.create(discordToken);

            discordClient.withGateway(gateway -> {
                EventService eventService = new EventService(gateway);
                return eventService.initializeEventListeners();
            }).block();

        } catch (Exception exception) {
            exception.printStackTrace();

            System.exit(1);
        }
    }

    public static CacheService getCacheService() {
        return cacheService;
    }

}
