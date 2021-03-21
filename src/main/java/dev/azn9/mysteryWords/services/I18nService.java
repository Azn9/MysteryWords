package dev.azn9.mysteryWords.services;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class I18nService {

    private static final Random                                                 RANDOM   = new Random();
    private static final Object2ObjectArrayMap<Locale, ResourceBundle>          MESSAGES = new Object2ObjectArrayMap<>();
    private static final Object2ObjectArrayMap<Locale, ObjectArrayList<String>> WORDS    = new Object2ObjectArrayMap<>();

    public boolean setupLocales() {
        for (Locale availableLocale : Locale.getAvailableLocales()) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", availableLocale);

            if (resourceBundle == null)
                continue;

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("words_" + availableLocale.toLanguageTag().toLowerCase() + ".txt");

            if (inputStream == null)
                continue;

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            WORDS.put(availableLocale, new ObjectArrayList<>(bufferedReader.lines().collect(Collectors.toList())));
            MESSAGES.put(availableLocale, resourceBundle);
        }

        return MESSAGES.size() > 0 && WORDS.size() > 0;
    }

    public ResourceBundle getMessages(Locale locale) {
        return MESSAGES.get(locale);
    }

    public String getNewWord(Locale locale) {
        if (!WORDS.containsKey(locale))
            return null;

        ObjectArrayList<String> list  = WORDS.get(locale);

        return list.get(RANDOM.nextInt(list.size()));
    }

}
