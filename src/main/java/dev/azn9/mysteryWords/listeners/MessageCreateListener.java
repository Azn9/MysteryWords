package dev.azn9.mysteryWords.listeners;

import dev.azn9.mysteryWords.config.Configuration;
import dev.azn9.mysteryWords.injector.Inject;
import dev.azn9.mysteryWords.services.CacheService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class MessageCreateListener {

    @Inject
    private static CacheService cacheService;

    public Mono<Object> accept(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();

        if (!message.getGuildId().isPresent())
            return Mono.empty();

        return cacheService.getGuildConfiguration(message.getGuildId().get().asLong()).flatMap(configuration -> {
            if (configuration.getChannelId() == message.getChannelId().asLong())
                return handleProposition(message, configuration).flatMap(__ -> Mono.empty());

            return Mono.empty();
        });
    }

    private Mono<Object> handleProposition(Message message, Configuration configuration) {
        String proposition = message.getContent();
        String realWord = configuration.getCurrentWord().toLowerCase();

        StringBuilder resultMessage = new StringBuilder();
        StringBuilder resultMessage2 = new StringBuilder();

        int index = 0;

        for (char c : proposition.toCharArray()) {
            String s = ("" + c).toLowerCase();

            if (s.matches("[a-z]")) {
                resultMessage.append(":regional_indicator_").append(s).append(": ");
                if (realWord.charAt(index) == c)
                    resultMessage2.append(":regional_indicator_").append(s).append(": ");
                else if (realWord.contains(s))
                    resultMessage2.append(":blue_circle:");
            } else {
                resultMessage.append(":question: ");
                resultMessage2.append(":red_circle:");
            }

            index++;
        }

        for (int i = 0; i < realWord.length() - resultMessage2.length(); i++)
            resultMessage2.append(":white_circle: ");

        return message.getChannel().flatMap(messageChannel -> messageChannel.createMessage(resultMessage.toString() + "\n" + resultMessage2.toString()).flatMap(message1 -> message.delete()));
    }
}
