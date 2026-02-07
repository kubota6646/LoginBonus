package me.kubota6646.loginbonus.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Bungeecord版LoginBonusプラグイン
 * Planのネットワークページに連続ログイン日数を表示するための軽量プラグイン
 */
public class BungeeMain extends Plugin {
    
    private BungeeMySqlReader storage;
    private Configuration config;
    
    @Override
    public void onEnable() {
        // 設定ファイルをロード
        loadConfig();
        
        // MySQLストレージを初期化（Bungeecordでは常にMySQLを使用）
        getLogger().info("MySQLストレージを初期化しています...");
        storage = new BungeeMySqlReader(this);
        storage.initialize();
        
        // Plan連携を登録
        registerPlanExtension();
        
        // デバッグコマンドを登録
        getProxy().getPluginManager().registerCommand(this, new DebugPlayerNamesCommand(this));
        
        getLogger().info("LoginBonus (Bungeecord) プラグインが有効化されました。");
        getLogger().info("Planのネットワークページに連続ログイン日数が表示されます。");
        getLogger().info("デバッグコマンド: /loginbonusdebug");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("LoginBonus (Bungeecord) を無効化しています...");
        
        // ストレージを閉じる
        if (storage != null) {
            storage.close();
        }
        
        getLogger().info("LoginBonus (Bungeecord) プラグインが無効化されました。");
    }
    
    /**
     * 設定ファイルをロード
     */
    private void loadConfig() {
        try {
            // データフォルダを作成
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            
            File configFile = new File(getDataFolder(), "config.yml");
            
            // 設定ファイルが存在しない場合、デフォルトをコピー
            if (!configFile.exists()) {
                try (InputStream in = getResourceAsStream("bungee-config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        getLogger().warning("デフォルト設定ファイル (bungee-config.yml) が見つかりません。");
                    }
                }
            }
            
            // 設定をロード
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            
        } catch (IOException e) {
            getLogger().severe("設定ファイルのロードに失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * 設定を取得
     */
    public Configuration getPluginConfig() {
        return config;
    }
    
    /**
     * ストレージを取得
     */
    public BungeeMySqlReader getStorage() {
        return storage;
    }
    
    /**
     * Plan Data Extensionを登録
     */
    private void registerPlanExtension() {
        BungeePlanHook planHook = new BungeePlanHook(this);
        planHook.register();
    }
}
