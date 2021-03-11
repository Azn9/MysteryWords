package dev.azn9.mysteryWords.services;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18nService {

    private static final Object2ObjectArrayMap<Locale, ResourceBundle> MESSAGES = new Object2ObjectArrayMap<>();

    public void setupLocales() {
        for (Locale availableLocale : Locale.getAvailableLocales()) {

        }
    }

}
