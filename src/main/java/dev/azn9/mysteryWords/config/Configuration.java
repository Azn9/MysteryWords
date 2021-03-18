package dev.azn9.mysteryWords.config;

public class Configuration {

    private Long    guildId;
    private long    channelId;
    private String  locale;
    private boolean leaderboardEnabled;
    private String  leaderboardType;

    public Configuration() {

    }

    public Configuration(Long guildId) {
        this.guildId = guildId;
    }

    public Configuration(Long guildId, long channelId, String locale, boolean leaderboardEnabled, String leaderboardType) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.locale = locale;
        this.leaderboardEnabled = leaderboardEnabled;
        this.leaderboardType = leaderboardType;
    }

    public Long getGuildId() {
        return this.guildId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public String getLocale() {
        return this.locale;
    }

    public boolean isLeaderboardEnabled() {
        return this.leaderboardEnabled;
    }

    public String getLeaderboardType() {
        return this.leaderboardType;
    }
}
