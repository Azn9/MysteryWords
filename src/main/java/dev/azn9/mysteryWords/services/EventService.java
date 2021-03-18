package dev.azn9.mysteryWords.services;

import dev.azn9.mysteryWords.listeners.GuildCreateListener;
import dev.azn9.mysteryWords.listeners.MessageCreateListener;
import dev.azn9.mysteryWords.listeners.ReadyListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class EventService {

    private final GatewayDiscordClient gateway;

    public EventService(@NonNull GatewayDiscordClient gateway) {
        this.gateway = gateway;
    }

    public Mono<Void> initializeEventListeners() {
        return Mono.when(
                gateway.on(ReadyEvent.class).flatMap(new ReadyListener()::accept),
                gateway.on(MessageCreateEvent.class).flatMap(new MessageCreateListener()::accept),
                gateway.on(GuildCreateEvent.class).flatMap(new GuildCreateListener()::accept)
        );
    }

}
