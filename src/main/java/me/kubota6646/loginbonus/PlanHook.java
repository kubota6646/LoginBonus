package me.kubota6646.loginbonus;

/**
 * Plan連携を管理するフッククラス
 * このクラスはPlan APIクラスを直接参照せず、リフレクションを使用して登録を行います。
 * これにより、Planがインストールされていない環境でもプラグインが正常にロードされます。
 */
public class PlanHook {
    
    private final Main plugin;
    
    public PlanHook(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Plan Data Extensionを登録します
     * @return 登録に成功した場合true、失敗した場合false
     */
    public boolean register() {
        try {
            // Planがインストールされているか確認
            if (plugin.getServer().getPluginManager().getPlugin("Plan") == null) {
                plugin.getLogger().info("Planプラグインが見つかりません。Plan連携は無効です。");
                return false;
            }
            
            // リフレクションを使用してPlan APIを呼び出す
            // これにより、PlanDataExtensionクラスがPlan APIクラスに依存していても、
            // このクラス自体はPlan APIに直接依存しないため、Planがない環境でもロードできます
            Class<?> extensionServiceClass = Class.forName("com.djrapitops.plan.extension.ExtensionService");
            Object extensionService = extensionServiceClass.getMethod("getInstance").invoke(null);
            
            // PlanDataExtensionのインスタンスを作成
            Class<?> planDataExtensionClass = Class.forName("me.kubota6646.loginbonus.PlanDataExtension");
            Object dataExtension = planDataExtensionClass.getConstructor(Main.class).newInstance(plugin);
            
            // ExtensionServiceに登録
            extensionServiceClass.getMethod("register", Class.forName("com.djrapitops.plan.extension.DataExtension"))
                .invoke(extensionService, dataExtension);
            
            plugin.getLogger().info("Plan連携が有効化されました。");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Planプラグインが見つかりません。Plan連携は無効です。");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Plan連携の登録に失敗しました: " + e.getMessage());
            return false;
        }
    }
}
