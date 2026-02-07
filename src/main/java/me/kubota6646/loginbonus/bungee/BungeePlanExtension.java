package me.kubota6646.loginbonus.bungee;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

import java.util.List;
import java.util.UUID;

/**
 * Bungeecord版Plan Data Extension
 * Planのネットワークページに連続ログイン日数ランキングを表示します。
 */
@PluginInfo(
    name = "LoginBonus", 
    iconName = "gift", 
    iconFamily = Family.SOLID, 
    color = Color.LIGHT_GREEN
)
public class BungeePlanExtension implements DataExtension {
    
    private final BungeeMain plugin;
    
    public BungeePlanExtension(BungeeMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
            CallEvents.SERVER_PERIODICAL,
            CallEvents.PLAYER_JOIN,
            CallEvents.PLAYER_LEAVE
        };
    }
    
    /**
     * ネットワーク全体の連続ログイン日数ランキングテーブル
     * Planのネットワークページに表示されます
     */
    @TableProvider(tableColor = Color.LIGHT_GREEN)
    public Table consecutiveLoginRanking() {
        BungeeMySqlReader storage = plugin.getStorage();
        
        Table.Factory table = Table.builder()
            .columnOne("プレイヤー", Icon.called("user").of(Family.SOLID).build())
            .columnTwo("連続ログイン日数", Icon.called("calendar-check").of(Family.SOLID).build());
        
        try {
            // 全プレイヤーのUUIDを取得
            List<UUID> allPlayers = storage.getAllPlayerUUIDs();
            
            // ストリーク日数が多い順にソートして上位を表示
            allPlayers.stream()
                .filter(uuid -> storage.getStreak(uuid) > 0) // ストリークが0より大きいプレイヤーのみ
                .sorted((uuid1, uuid2) -> {
                    int streak1 = storage.getStreak(uuid1);
                    int streak2 = storage.getStreak(uuid2);
                    return Integer.compare(streak2, streak1); // 降順
                })
                .limit(50) // 上位50名まで表示
                .forEach(uuid -> {
                    String playerName = null;
                    
                    // 1. データベースから保存されたプレイヤー名を取得
                    playerName = storage.getPlayerName(uuid);
                    
                    // 2. データベースに名前がない場合、Bungeecordから現在のプレイヤー名を取得を試みる
                    if (playerName == null || playerName.isEmpty()) {
                        try {
                            net.md_5.bungee.api.connection.ProxiedPlayer player = 
                                plugin.getProxy().getPlayer(uuid);
                            if (player != null) {
                                playerName = player.getName();
                            }
                        } catch (Exception e) {
                            // プレイヤーが見つからない場合は無視
                        }
                    }
                    
                    // 3. どちらからも取得できない場合はUUIDの短縮版を使用
                    if (playerName == null || playerName.isEmpty()) {
                        playerName = uuid.toString().substring(0, 8) + "...";
                    }
                    
                    int streak = storage.getStreak(uuid);
                    table.addRow(playerName, streak);
                });
        } catch (Exception e) {
            plugin.getLogger().warning("連続ログイン日数ランキングの取得に失敗しました: " + e.getMessage());
        }
        
        return table.build();
    }
    
    /**
     * プレイヤー個別の連続ログイン日数
     * Planのプレイヤーページにも表示されます
     */
    @NumberProvider(
        text = "連続ログイン日数",
        description = "プレイヤーの現在の連続ログイン日数（ストリーク）",
        priority = 100,
        iconName = "calendar-check",
        iconFamily = Family.SOLID,
        iconColor = Color.GREEN,
        showInPlayerTable = true
    )
    public long consecutiveLoginDays(UUID playerUUID) {
        BungeeMySqlReader storage = plugin.getStorage();
        return storage.getStreak(playerUUID);
    }
}
