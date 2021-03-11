package dev.azn9.mysteryWords.services;

import dev.azn9.mysteryWords.config.Configuration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import reactor.core.publisher.Mono;

public class DatabaseService {

    private final String databaseHost;
    private final String databaseName;
    private final String databaseUser;
    private final String databasePass;

    private Connection connection;

    public DatabaseService(String databaseHost, String databaseName, String databaseUser, String databasePass) throws SQLException, IOException {
        this.databaseHost = databaseHost;
        this.databaseName = databaseName;
        this.databaseUser = databaseUser;
        this.databasePass = databasePass;

        openConnection();
    }

    private void openConnection() throws SQLException, IOException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" + databaseHost + "/" + databaseName + "?autoReconnect=true", databaseUser, databasePass);

        if (this.connection == null || this.connection.isClosed())
            throw new SQLException();

        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT TABLE_NAME\n" +
                "FROM INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = ?");

        preparedStatement.setString(1, this.databaseName);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next())
            createDefaultDatabase();

        //TODO add validation for existing tables & updates
    }

    private void createDefaultDatabase() throws IOException {
        System.out.println("[INFO] Creating default database...");

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mysteryword.sql");

        assert inputStream != null : "The default configuration file cannot be found !";

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            bufferedReader.lines().forEach(line -> {
                try {
                    this.connection.prepareStatement(line).execute();
                } catch (SQLException throwables) {
                    System.out.println("[DEBUG] " + line);
                    throwables.printStackTrace();
                }
            });
        }
    }

    public Mono<Configuration> getGuildConfiguration(Long guildId) {
        return Mono.create(sink -> {
            try {
                if (this.connection == null || this.connection.isClosed())
                    openConnection();

                if (this.connection == null || this.connection.isClosed())
                    sink.error(new IllegalStateException());

                PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM ?.server_settings WHERE GUILD_ID = ?");
                preparedStatement.setString(1, this.databaseName);
                preparedStatement.setLong(2, guildId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next())
                    sink.success(new Configuration(guildId, resultSet.getLong("CHANNEL_ID"), resultSet.getString("LOCALE"), resultSet.getBoolean("LEADERBOARD_ENABLED"), resultSet.getString("LEADERBOARD_TYPE")));
            } catch (SQLException | IOException exception) {
                sink.error(exception);
            }
        });
    }

    public Connection getConnection() {
        return this.connection;
    }
}
