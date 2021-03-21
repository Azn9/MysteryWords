package dev.azn9.mysteryWords.config;

import java.util.Locale;

public class Configuration {

    private transient Locale locale;

    private Long    guildId            = 0L;
    private Long    channelId          = 0L;
    private String  localeTag          = "fr";
    private boolean leaderboardEnabled = true;
    private String  leaderboardType    = "top_all";
    private String  currentWord        = ""; //TODO

    public Configuration() {

    }

    public Configuration(Long guildId) {
        this.guildId = guildId;
        this.locale = Locale.FRENCH;
        this.localeTag = "fr";
    }

    public Configuration(Long guildId, long channelId, Locale locale, boolean leaderboardEnabled, String leaderboardType) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.locale = locale;
        this.localeTag = locale.toLanguageTag();
        this.leaderboardEnabled = leaderboardEnabled;
        this.leaderboardType = leaderboardType;
    }

    public Long getGuildId() {
        return this.guildId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public Locale getLocale() {
        if (this.locale == null)
            this.locale = Locale.forLanguageTag(this.localeTag);

        return this.locale;
    }

    public boolean isLeaderboardEnabled() {
        return this.leaderboardEnabled;
    }

    public String getLeaderboardType() {
        return this.leaderboardType;
    }

    public String getCurrentWord() {
        return this.currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public String getLocaleTag() {
        return this.localeTag;
    }
}
