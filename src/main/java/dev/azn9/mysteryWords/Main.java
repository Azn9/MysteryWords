package dev.azn9.mysteryWords;

import dev.azn9.mysteryWords.injector.Injector;
import dev.azn9.mysteryWords.services.CacheService;
import dev.azn9.mysteryWords.services.DatabaseService;
import dev.azn9.mysteryWords.services.EventService;
import dev.azn9.mysteryWords.services.I18nService;
import discord4j.core.DiscordClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;

public class Main {

    public static void main(String[] args) {
        try {
            Injector injector = new Injector();

            I18nService i18nService = new I18nService();
            assert i18nService.setupLocales() : "An error occured while loading the translations !";
            injector.registerInjection(i18nService);

            String discordToken = System.getenv("DISCORD_TOKEN");
            String databaseHost = System.getenv("DATABASE_HOST");
            String databaseName = System.getenv("DATABASE_NAME");
            String databaseUser = System.getenv("DATABASE_USER");
            String databasePass = System.getenv("DATABASE_PASS");
            String redisUrl = System.getenv("REDIS_URL");

            assert discordToken != null && !discordToken.isEmpty() : "You didn't provide the discord bot token !";
            assert databaseHost != null && !databaseHost.isEmpty() : "You didn't provide the database host url !";
            assert databaseName != null && !databaseName.isEmpty() : "You didn't provide the database name !";
            assert databaseUser != null && !databaseUser.isEmpty() : "You didn't provide the database user !";
            assert databasePass != null : "You didn't provide the database password !";
            assert redisUrl != null && !redisUrl.isEmpty() : "You didn't provide the redis url !";

            DatabaseService databaseService = new DatabaseService(databaseHost, databaseName, databaseUser, databasePass);
            assert databaseService.getConnection() != null : "An error occurred while attempting to connect to the database !";
            injector.registerInjection(databaseService);

            Config config = new Config();
            config.useSingleServer().setAddress(redisUrl);
            RedissonReactiveClient redissonReactive = Redisson.createReactive(config);
            assert redissonReactive != null : "An error occurred while attempting to connect to the redis server !";
            injector.registerInjection(redissonReactive);

            CacheService cacheService = new CacheService();
            injector.registerInjection(cacheService);

            DiscordClient discordClient = DiscordClient.create(discordToken);
            injector.registerInjection(discordClient);

            discordClient.withGateway(gateway -> {
                injector.registerInjection(gateway);

                injector.startInjection();

                EventService eventService = new EventService();
                return eventService.initializeEventListeners();
            }).block();
        } catch (Exception exception) {
            exception.printStackTrace();

            System.exit(1);
        }
    }

}
