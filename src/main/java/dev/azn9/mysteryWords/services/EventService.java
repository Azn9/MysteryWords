package dev.azn9.mysteryWords.services;

import dev.azn9.mysteryWords.injector.Inject;
import dev.azn9.mysteryWords.listeners.CommandListener;
import dev.azn9.mysteryWords.listeners.GuildCreateListener;
import dev.azn9.mysteryWords.listeners.MessageCreateListener;
import dev.azn9.mysteryWords.listeners.ReactionListener;
import dev.azn9.mysteryWords.listeners.ReadyListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;

public class EventService {

    @Inject
    private static GatewayDiscordClient gateway;

    public Mono<Void> initializeEventListeners() {
        ReadyListener readyListener = new ReadyListener();
        MessageCreateListener messageCreateListener = new MessageCreateListener();
        CommandListener commandListener = new CommandListener();
        ReactionListener reactionListener = new ReactionListener();
        GuildCreateListener guildCreateListener = new GuildCreateListener();

        return Mono.when(
                gateway.on(ReadyEvent.class).flatMap(readyListener::accept),
                gateway.on(GuildCreateEvent.class).flatMap(guildCreateListener::accept),

                gateway.on(ReactionAddEvent.class).flatMap(reactionAddEvent -> {
                    if (reactionAddEvent.getMember().isEmpty() || reactionAddEvent.getMember().get().isBot())
                        return Mono.empty();
                    else
                        return reactionAddEvent.getMessage().flatMap(message -> {
                            if (message.getAuthor().isEmpty() || !message.getAuthor().get().getId().equals(gateway.getSelfId()))
                                return Mono.empty();
                            else
                                return reactionListener.accept(reactionAddEvent);
                        });
                }),

                gateway.on(MessageCreateEvent.class).flatMap(messageCreateEvent -> {
                    if (messageCreateEvent.getMessage().getAuthor().isEmpty() || messageCreateEvent.getMessage().getAuthor().get().isBot())
                        return Mono.empty();

                    if (messageCreateEvent.getMessage().getContent().startsWith("/"))
                        return commandListener.accept(messageCreateEvent);
                    else
                        return messageCreateListener.accept(messageCreateEvent);
                })
        );
    }

}
