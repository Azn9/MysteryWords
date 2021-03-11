package dev.azn9.mysteryWords.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class MessageCreateListener {

    public <R> Publisher<? extends R> accept(MessageCreateEvent messageCreateEvent) {
        //TODO

        return Mono.empty();
    }
}
