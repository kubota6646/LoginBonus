package me.kubota6646.loginbonus;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import me.kubota6646.loginbonus.storage.StorageInterface;

import java.util.List;
import java.util.UUID;

@PluginInfo(name = "LoginBonus", iconName = "gift", iconFamily = Family.SOLID, color = Color.LIGHT_GREEN)
public class PlanDataExtension implements DataExtension {
    
    private final Main plugin;
    
    public PlanDataExtension(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
            CallEvents.PLAYER_JOIN,
            CallEvents.PLAYER_LEAVE,
            CallEvents.SERVER_PERIODICAL
        };
    }
    
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
        StorageInterface storage = plugin.getStorage();
        return storage.getStreak(playerUUID);
    }
    
    @TableProvider(tableColor = Color.LIGHT_GREEN)
    public Table consecutiveLoginRanking() {
        StorageInterface storage = plugin.getStorage();
        
        Table.Factory table = Table.builder()
            .columnOne("プレイヤー", Icon.called("user").of(Family.SOLID).build())
            .columnTwo("連続ログイン日数", Icon.called("calendar-check").of(Family.SOLID).build());
        
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
                String playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
                if (playerName == null) {
                    playerName = uuid.toString();
                }
                int streak = storage.getStreak(uuid);
                table.addRow(playerName, streak);
            });
        
        return table.build();
    }
}
