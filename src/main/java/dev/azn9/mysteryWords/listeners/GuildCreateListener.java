package dev.azn9.mysteryWords.listeners;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.rest.util.Permission;
import org.reactivestreams.Publisher;

public class GuildCreateListener {

    public <R> Publisher<? extends R> accept(GuildCreateEvent guildCreateEvent) {
        return guildCreateEvent.getGuild().getChannels().filterWhen(guildChannel ->
                guildChannel.getEffectivePermissions(guildCreateEvent.getClient().getSelfId()).map(permissions ->
                        permissions.contains(Permission.SEND_MESSAGES)))
                .flatMap(guildChannel -> {

                });
    }
}
