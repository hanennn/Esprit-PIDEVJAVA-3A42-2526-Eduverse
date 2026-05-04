package org.example.services;

import org.example.utils.MyConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class BadwordDatabaseBootstrap {
    private static boolean initialized;

    private BadwordDatabaseBootstrap() {}

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        try (Connection connection = MyConnection.getInstance().getCnx()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS badword ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "word VARCHAR(255) NOT NULL UNIQUE,"
                        + "action ENUM('MASK', 'BLOCK', 'ALERT') NOT NULL DEFAULT 'MASK',"
                        + "active BOOLEAN NOT NULL DEFAULT 1,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

                statement.execute("CREATE TABLE IF NOT EXISTS badword_log ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "user_id INT NOT NULL,"
                        + "violated_word VARCHAR(255) NOT NULL,"
                        + "action ENUM('MASK', 'BLOCK', 'ALERT') NOT NULL,"
                        + "content LONGTEXT NOT NULL,"
                        + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT IGNORE INTO badword (word, action, active) VALUES (?, ?, 1)")) {
                seed(statement, "spam", "BLOCK");
                seed(statement, "hack", "BLOCK");
                seed(statement, "fraud", "ALERT");
                seed(statement, "hate", "ALERT");
                seed(statement, "violence", "ALERT");
                seed(statement, "xxx", "BLOCK");
                seed(statement, "malware", "BLOCK");
                seed(statement, "phishing", "BLOCK");
            }

            ensureIndex(connection, "badword", "idx_badword_word", "word");
            ensureIndex(connection, "badword", "idx_badword_active", "active");
            ensureIndex(connection, "badword_log", "idx_badword_log_user", "user_id");
            ensureIndex(connection, "badword_log", "idx_badword_log_timestamp", "timestamp");

            // Assurer que les colonnes Image/GIF existent
            try (Statement stmt = connection.createStatement()) {
                try { stmt.execute("ALTER TABLE sujet ADD COLUMN image_url VARCHAR(255) AFTER contenu"); } catch (SQLException ignore) {}
                try { stmt.execute("ALTER TABLE message ADD COLUMN gif_url VARCHAR(255) AFTER contenu"); } catch (SQLException ignore) {}
            }

            initialized = true;
        } catch (SQLException exception) {
            System.err.println("Impossible d'initialiser automatiquement le système badword: " + exception.getMessage());
        }
    }

    private static void seed(PreparedStatement statement, String word, String action) throws SQLException {
        statement.setString(1, word);
        statement.setString(2, action);
        statement.executeUpdate();
    }

    private static void ensureIndex(Connection connection, String tableName, String indexName, String columnName) throws SQLException {
        if (indexExists(connection, tableName, indexName)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX " + indexName + " ON " + tableName + "(" + columnName + ")");
        }
    }

    private static boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (resultSet.next()) {
                String existingIndexName = resultSet.getString("INDEX_NAME");
                if (existingIndexName != null && existingIndexName.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }
        return false;
    }
}