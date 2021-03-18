package dev.azn9.mysteryWords.services;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18nService {

    private static final Object2ObjectArrayMap<Locale, ResourceBundle> MESSAGES = new Object2ObjectArrayMap<>();

    public boolean setupLocales() {
        for (Locale availableLocale : Locale.getAvailableLocales()) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", availableLocale);

            if (resourceBundle == null)
                continue;

            MESSAGES.put(availableLocale, resourceBundle);
        }

        return MESSAGES.size() > 0;
    }

    public ResourceBundle getMessages(Locale locale) {
        return MESSAGES.get(locale);
    }

}
