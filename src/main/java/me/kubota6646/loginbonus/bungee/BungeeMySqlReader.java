package me.kubota6646.loginbonus.bungee;

import net.md_5.bungee.config.Configuration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bungeecord用の軽量MySQL読み取りクラス
 * Planのネットワークページ表示のために連続ログイン日数を読み取ります
 */
public class BungeeMySqlReader {
    
    private final BungeeMain plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String tableName;
    private final String username;
    private final String password;
    
    public BungeeMySqlReader(BungeeMain plugin) {
        this.plugin = plugin;
        Configuration config = plugin.getPluginConfig();
        
        this.host = config.getString("mysql.host", "localhost");
        this.port = config.getInt("mysql.port", 3306);
        this.database = config.getString("mysql.database", "loginbonus");
        
        // テーブル名のバリデーション（SQLインジェクション対策）
        String configTableName = config.getString("mysql.table-name", "player_data");
        if (!configTableName.matches("^[a-zA-Z0-9_]+$")) {
            plugin.getLogger().warning("無効なテーブル名: " + configTableName + " - デフォルトの 'player_data' を使用します");
            this.tableName = "player_data";
        } else {
            this.tableName = configTableName;
        }
        
        this.username = config.getString("mysql.username", "root");
        this.password = config.getString("mysql.password", "password");
    }
    
    /**
     * MySQL接続を初期化
     */
    public void initialize() {
        try {
            // MySQL接続URLを構築
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    host, port, database);
            
            // 接続を確立
            connection = DriverManager.getConnection(url, username, password);
            
            plugin.getLogger().info("MySQLデータベースに接続しました");
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQLデータベースの接続に失敗しました: " + e.getMessage());
            plugin.getLogger().severe("データベースの接続情報とデータベースが存在することを確認してください。");
        }
    }
    
    /**
     * 接続が切れている場合に再接続
     */
    private void reconnectIfNeeded() throws SQLException {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(1)) {
                plugin.getLogger().warning("MySQL接続が切断されました。再接続を試みます...");
                initialize();
                if (connection == null || connection.isClosed()) {
                    throw new SQLException("MySQL再接続に失敗しました。");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL再接続チェックに失敗しました: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * プレイヤーのストリークを取得
     */
    public int getStreak(UUID playerId) {
        try {
            reconnectIfNeeded();
            
            String sql = "SELECT streak FROM " + tableName + " WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("streak");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("ストリークの取得に失敗しました: " + e.getMessage());
        }
        return 1; // デフォルト値
    }
    
    /**
     * 全プレイヤーのUUIDを取得
     */
    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> players = new ArrayList<>();
        try {
            reconnectIfNeeded();
            
            String sql = "SELECT uuid FROM " + tableName;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    try {
                        players.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("無効なUUID: " + uuidStr);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("プレイヤーUUIDの取得に失敗しました: " + e.getMessage());
        }
        return players;
    }
    
    /**
     * 接続を閉じる
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("MySQLデータベース接続を閉じました");
            } catch (SQLException e) {
                plugin.getLogger().warning("MySQLデータベース接続のクローズに失敗しました: " + e.getMessage());
            }
        }
    }
}
