package dev.azn9.mysteryWords.listeners;

import dev.azn9.mysteryWords.config.Configuration;
import dev.azn9.mysteryWords.injector.Inject;
import dev.azn9.mysteryWords.services.CacheService;
import dev.azn9.mysteryWords.services.I18nService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import reactor.core.publisher.Mono;

public class ReactionListener {

    @Inject
    private static CacheService cacheService;

    @Inject
    private static I18nService i18nService;

    public Mono<Object> accept(ReactionAddEvent reactionAddEvent) {
        return reactionAddEvent.getMessage().flatMap(message -> {
            if (!message.getGuildId().isPresent())
                return Mono.empty();

            if (message.getEmbeds().size() != 1)
                return Mono.empty();

            Optional<String> optionalTitle = message.getEmbeds().get(0).getTitle();

            return optionalTitle.map(s -> cacheService.getGuildConfiguration(message.getGuildId().get().asLong()).flatMap(configuration -> {
                if (!s.equalsIgnoreCase(i18nService.getMessages(configuration.getLocale()).getString("setup_embed_title")))
                    return Mono.empty();

                return handleSettingsInteraction(reactionAddEvent, message, configuration);
            })).orElseGet(Mono::empty);

        });
    }

    private Mono<Object> handleSettingsInteraction(ReactionAddEvent reactionAddEvent, Message message, Configuration configuration) {
        Optional<Unicode> unicodeOptional = reactionAddEvent.getEmoji().asUnicodeEmoji();
        if (!unicodeOptional.isPresent())
            return Mono.empty();

        String rawUnicode = unicodeOptional.get().getRaw();
        if (rawUnicode.equalsIgnoreCase(configuration.getFlagTag())) {
            return message.removeAllReactions().then(message.edit(messageEditSpec -> messageEditSpec.setEmbed(embedCreateSpec -> getLangEmbed(configuration, embedCreateSpec)))).flatMap(message1 -> buildMono(message1, i18nService.getLocaleEmojis(), message.addReaction(ReactionEmoji.unicode("⬅️"))));
        } else
            switch (rawUnicode) {
                case "\uD83D\uDCAC":
                    return message.removeAllReactions().then(message.edit(messageEditSpec -> messageEditSpec.setEmbed(embedCreateSpec -> getChannelEmbed(configuration, embedCreateSpec)))).flatMap(message1 -> message.addReaction(ReactionEmoji.unicode("⬅️")));

                case "⬅️":
                default:
                    return Mono.empty();
            }
    }

    private Mono<?> buildMono(Message message, List<Unicode> localeEmojis, Mono<?> base) {
        if (localeEmojis.size() == 0)
            return base;

        Unicode unicode = localeEmojis.remove(0);

        return buildMono(message, localeEmojis, base.then(message.addReaction(unicode)));
    }

    private void getLangEmbed(Configuration configuration, EmbedCreateSpec embedCreateSpec) {
        ResourceBundle messages = i18nService.getMessages(configuration.getLocale());

        embedCreateSpec.setColor(Color.CINNABAR);
        embedCreateSpec.setTitle(messages.getString("setup_embed_lang_title"));
        embedCreateSpec.setDescription(String.format(messages.getString("setup_embed_lang_description"), i18nService.getLanguageEmbedBody()));
    }

    private void getChannelEmbed(Configuration configuration, EmbedCreateSpec embedCreateSpec) {

    }

}
