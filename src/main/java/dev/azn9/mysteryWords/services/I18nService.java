package dev.azn9.mysteryWords.services;

import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class I18nService {

    private static final Random                                                 RANDOM           = new Random();
    private static final Object2ObjectArrayMap<Locale, ResourceBundle>          MESSAGES         = new Object2ObjectArrayMap<>();
    private static final Object2ObjectArrayMap<Locale, ObjectArrayList<String>> WORDS            = new Object2ObjectArrayMap<>();
    private static final Object2ObjectArrayMap<Locale, Unicode>                 EMOJIS_BY_LOCALE = new Object2ObjectArrayMap<>();

    private String languageEmbedBody;

    public boolean setupLocales() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Locale availableLocale : Locale.getAvailableLocales()) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", availableLocale);

            if (resourceBundle == null)
                continue;

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("words_" + availableLocale.toLanguageTag().toLowerCase() + ".txt");

            if (inputStream == null)
                continue;

            String name = availableLocale.getDisplayName(availableLocale);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

            stringBuilder.append("- :flag_").append(resourceBundle.getString("emoji_tag")).append(": ").append(name).append("\n");
            EMOJIS_BY_LOCALE.put(availableLocale, Unicode.unicode(resourceBundle.getString("emoji_tag").chars().map(i -> i  - 96 + 56805).mapToObj(s -> "\uD83C" + (char) s).collect(Collectors.joining())));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            WORDS.put(availableLocale, new ObjectArrayList<>(bufferedReader.lines().collect(Collectors.toList())));
            MESSAGES.put(availableLocale, resourceBundle);
        }

        this.languageEmbedBody = stringBuilder.toString();

        return MESSAGES.size() > 0 && WORDS.size() > 0;
    }

    public ResourceBundle getMessages(Locale locale) {
        return MESSAGES.get(locale);
    }

    public String getNewWord(Locale locale) {
        if (!WORDS.containsKey(locale))
            return null;

        ObjectArrayList<String> list = WORDS.get(locale);

        return list.get(RANDOM.nextInt(list.size()));
    }

    public ObjectSet<Locale> getAvailableLocales() {
        return MESSAGES.keySet();
    }

    public String getLanguageEmbedBody() {
        return this.languageEmbedBody;
    }

    public List<Unicode> getLocaleEmojis() {
        return new ArrayList<>(EMOJIS_BY_LOCALE.values());
    }
}
