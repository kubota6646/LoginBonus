package me.kubota6646.loginbonus.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.UUID;

/**
 * プレイヤー名のデバッグコマンド
 * データベースからプレイヤー名が正しく読み取れているかを確認するためのコマンド
 */
public class DebugPlayerNamesCommand extends Command {
    
    private final BungeeMain plugin;
    
    public DebugPlayerNamesCommand(BungeeMain plugin) {
        super("loginbonusdebug", "loginbonus.admin");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginbonus.admin")) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== LoginBonus Debug Info ===");
        
        BungeeMySqlReader storage = plugin.getStorage();
        if (storage == null) {
            sender.sendMessage(ChatColor.RED + "ストレージが初期化されていません。");
            return;
        }
        
        // 全プレイヤーのUUIDを取得
        List<UUID> allPlayers = storage.getAllPlayerUUIDs();
        sender.sendMessage(ChatColor.YELLOW + "データベース内のプレイヤー数: " + allPlayers.size());
        
        // 上位10名のプレイヤー情報を表示
        int count = 0;
        int withName = 0;
        int withoutName = 0;
        
        sender.sendMessage(ChatColor.YELLOW + "=== 上位10名のプレイヤー情報 ===");
        
        for (UUID uuid : allPlayers) {
            if (count >= 10) break;
            
            String playerName = storage.getPlayerName(uuid);
            int streak = storage.getStreak(uuid);
            
            if (playerName != null && !playerName.isEmpty()) {
                withName++;
                sender.sendMessage(ChatColor.GREEN + "✓ " + playerName + " (UUID: " + uuid.toString().substring(0, 8) + "..., Streak: " + streak + ")");
            } else {
                withoutName++;
                sender.sendMessage(ChatColor.RED + "✗ [名前なし] (UUID: " + uuid.toString().substring(0, 8) + "..., Streak: " + streak + ")");
            }
            
            count++;
        }
        
        // 統計を計算
        sender.sendMessage(ChatColor.YELLOW + "=== 全体統計（サンプル10名） ===");
        sender.sendMessage(ChatColor.GREEN + "名前あり: " + withName);
        sender.sendMessage(ChatColor.RED + "名前なし: " + withoutName);
        
        // 推奨アクション
        if (withoutName > 0) {
            sender.sendMessage(ChatColor.YELLOW + "");
            sender.sendMessage(ChatColor.YELLOW + "推奨アクション:");
            sender.sendMessage(ChatColor.YELLOW + "1. Bukkitサーバーで /rewardmigratenames を実行してください");
            sender.sendMessage(ChatColor.YELLOW + "2. データベース接続設定が正しいか確認してください");
            sender.sendMessage(ChatColor.YELLOW + "3. プレイヤーが一度でもサーバーに参加したことがあるか確認してください");
        } else {
            sender.sendMessage(ChatColor.GREEN + "");
            sender.sendMessage(ChatColor.GREEN + "✓ 全てのプレイヤーに名前が設定されています！");
            sender.sendMessage(ChatColor.YELLOW + "まだPlanで名前が表示されない場合:");
            sender.sendMessage(ChatColor.YELLOW + "1. Planのキャッシュをクリアしてください");
            sender.sendMessage(ChatColor.YELLOW + "2. Bungeecordを再起動してください");
            sender.sendMessage(ChatColor.YELLOW + "3. ブラウザのキャッシュをクリアしてください (Ctrl+Shift+R)");
        }
    }
}
