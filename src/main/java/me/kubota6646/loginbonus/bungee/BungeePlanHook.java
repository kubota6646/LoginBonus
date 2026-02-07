package me.kubota6646.loginbonus.bungee;

/**
 * Bungeecord版Plan連携を管理するフッククラス
 * PlanがBungeecordにインストールされている場合、ネットワークページに連続ログイン日数を表示します。
 */
public class BungeePlanHook {
    
    private final BungeeMain plugin;
    
    public BungeePlanHook(BungeeMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Plan Data Extensionを登録します
     * @return 登録に成功した場合true、失敗した場合false
     */
    public boolean register() {
        try {
            // PlanがBungeecordにインストールされているか確認
            if (plugin.getProxy().getPluginManager().getPlugin("Plan") == null) {
                plugin.getLogger().info("Planプラグインが見つかりません。Plan連携は無効です。");
                return false;
            }
            
            // リフレクションを使用してPlan APIを呼び出す
            Class<?> extensionServiceClass = Class.forName("com.djrapitops.plan.extension.ExtensionService");
            Object extensionService = extensionServiceClass.getMethod("getInstance").invoke(null);
            
            // BungeePlanExtensionのインスタンスを作成
            Class<?> planExtensionClass = Class.forName("me.kubota6646.loginbonus.bungee.BungeePlanExtension");
            Object dataExtension = planExtensionClass.getConstructor(BungeeMain.class).newInstance(plugin);
            
            // ExtensionServiceに登録
            extensionServiceClass.getMethod("register", Class.forName("com.djrapitops.plan.extension.DataExtension"))
                .invoke(extensionService, dataExtension);
            
            plugin.getLogger().info("Plan連携が有効化されました。ネットワークページに連続ログイン日数が表示されます。");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Plan APIが見つかりません: " + e.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Plan連携の登録に失敗しました: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
