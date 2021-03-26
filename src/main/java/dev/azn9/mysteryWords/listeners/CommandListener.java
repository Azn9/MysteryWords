package dev.azn9.mysteryWords.listeners;

import dev.azn9.mysteryWords.config.Configuration;
import dev.azn9.mysteryWords.injector.Inject;
import dev.azn9.mysteryWords.services.CacheService;
import dev.azn9.mysteryWords.services.I18nService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import java.util.ResourceBundle;
import reactor.core.publisher.Mono;

public class CommandListener {

    @Inject
    private static CacheService cacheService;

    @Inject
    private static I18nService i18nService;

    public Mono<Object> accept(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();

        if (!message.getGuildId().isPresent())
            return Mono.empty();

        if (message.getContent().equalsIgnoreCase("/settings") || message.getContent().equalsIgnoreCase("/parametres"))
            return handleSettingsCommand(message);

        return Mono.empty();
    }

    private Mono<Object> handleSettingsCommand(Message message) {
        return cacheService.getGuildConfiguration(message.getGuildId().get().asLong()).flatMap(configuration -> {
            ResourceBundle messages = i18nService.getMessages(configuration.getLocale());

            return message.getAuthorAsMember().flatMap(member ->
                    member.getBasePermissions().flatMap(permissions -> {
                        if (permissions.contains(Permission.ADMINISTRATOR) || permissions.contains(Permission.MANAGE_CHANNELS)) {
                            return message.getGuild().flatMap(guild -> {
                                if (configuration.getChannelId() != 0L)
                                    return guild.getChannelById(Snowflake.of(configuration.getChannelId())).map(GuildChannel::getMention);
                                else
                                    return Mono.just(messages.getString("channel_not_set"));
                            }).flatMap(gameChannel -> message.getChannel().flatMap(messageChannel ->
                                    messageChannel.createEmbed(embedCreateSpec -> settingsMainPage(embedCreateSpec, messages, configuration, gameChannel))
                                            .flatMap(message1 -> message1.addReaction(ReactionEmoji.unicode(configuration.getFlagTag())).then(message1.addReaction(ReactionEmoji.unicode("\uD83D\uDCAC"))))));
                        } else {
                            return message.getChannel().flatMap(messageChannel ->
                                    messageChannel.createEmbed(embedCreateSpec -> {
                                        embedCreateSpec.setColor(Color.RED);
                                        embedCreateSpec.setTitle(messages.getString("no_permission_title"));
                                        embedCreateSpec.setDescription(messages.getString("no_permission_description"));
                                    }));
                        }
                    }));
        });
    }

    public void settingsMainPage(EmbedCreateSpec embedCreateSpec, ResourceBundle messages, Configuration configuration, String gameChannel) {
        embedCreateSpec.setColor(Color.TAHITI_GOLD);
        embedCreateSpec.setTitle(messages.getString("setup_embed_title"));

        String localeName = configuration.getLocale().getDisplayName(configuration.getLocale());
        localeName = localeName.substring(0, 1).toUpperCase(configuration.getLocale()) + localeName.substring(1);

        embedCreateSpec.setDescription(messages.getString("setup_embed_description"));

        embedCreateSpec.addField(":flag_" + configuration.getLocaleTag() + ": " + messages.getString("setup_embed_field_lang"), localeName, false);
        embedCreateSpec.addField(":speech_balloon: " + messages.getString("setup_embed_field_channel"), gameChannel, false);

        embedCreateSpec.addField(":hourglass: " + messages.getString("setup_embed_field_soon_title"), messages.getString("setup_embed_field_soon_description"), false);
    }

}
