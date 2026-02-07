package me.kubota6646.loginbonus;

import me.kubota6646.loginbonus.storage.MySqlStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * プレイヤー名の移行コマンド
 * データベース内の空のplayer_name列をBukkitのオフラインプレイヤーキャッシュから取得した名前で埋めます
 */
public class RewardMigrateNamesCommand implements CommandExecutor {
    
    private final Main plugin;
    
    public RewardMigrateNamesCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("loginbonus.admin") && !sender.isOp()) {
            sender.sendMessage(plugin.getMessage("no-permission-admin", 
                "&cこのコマンドを実行する権限がありません。"));
            return true;
        }
        
        // MySQLストレージの場合のみ実行可能
        if (!(plugin.getStorage() instanceof MySqlStorage)) {
            sender.sendMessage(plugin.getMessage("migrate-names-mysql-only",
                "&cこのコマンドはMySQLストレージを使用している場合のみ実行できます。",
                ""));
            return true;
        }
        
        MySqlStorage storage = (MySqlStorage) plugin.getStorage();
        
        sender.sendMessage(plugin.getMessage("migrate-names-start",
            "&aプレイヤー名の移行を開始します...",
            ""));
        
        // 非同期で実行（データベース操作のため）
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int updatedCount = storage.migratePlayerNames();
                
                // 結果をメインスレッドで送信
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getMessage("migrate-names-complete",
                        "&aプレイヤー名の移行が完了しました。更新数: %count% 人",
                        "%count%", String.valueOf(updatedCount)));
                });
            } catch (Exception e) {
                plugin.getLogger().severe("プレイヤー名の移行中にエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
                
                // エラーをメインスレッドで送信
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(plugin.getMessage("migrate-names-error",
                        "&cプレイヤー名の移行中にエラーが発生しました: %error%",
                        "%error%", e.getMessage()));
                });
            }
        });
        
        return true;
    }
}
