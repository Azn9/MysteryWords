package dev.azn9.mysteryWords.listeners;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ReadyListener {

    public <R> Publisher<? extends R> accept(ReadyEvent readyEvent) {


        return Mono.empty();
    }
}
