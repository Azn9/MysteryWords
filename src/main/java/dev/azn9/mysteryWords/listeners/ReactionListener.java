package dev.azn9.mysteryWords.listeners;

import dev.azn9.mysteryWords.config.Configuration;
import dev.azn9.mysteryWords.injector.Inject;
import dev.azn9.mysteryWords.services.CacheService;
import dev.azn9.mysteryWords.services.I18nService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import java.util.Optional;
import reactor.core.publisher.Mono;

public class ReactionListener {

    @Inject
    private static CacheService cacheService;

    @Inject
    private static I18nService i18nService;

    public Mono<Object> accept(ReactionAddEvent reactionAddEvent) {
        return reactionAddEvent.getMessage().flatMap(message -> {
            if (message.getGuildId().isEmpty())
                return Mono.empty();

            if (message.getEmbeds().size() != 1)
                return Mono.empty();

            Optional<String> optionalTitle = message.getEmbeds().get(0).getTitle();

            if (optionalTitle.isEmpty())
                return Mono.empty();

            return cacheService.getGuildConfiguration(message.getGuildId().get().asLong()).flatMap(configuration -> {
                if (!optionalTitle.get().equalsIgnoreCase(i18nService.getMessages(configuration.getLocale()).getString("setup_embed_title")))
                    return Mono.empty();

                return handleSettingsInteraction(reactionAddEvent, message, configuration);
            });
        });
    }

    private Mono<Object> handleSettingsInteraction(ReactionAddEvent reactionAddEvent, Message message, Configuration configuration) {


        return Mono.empty();
    }

}
