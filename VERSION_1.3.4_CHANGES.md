# LoginBonus バージョン 1.3.4 変更点

## リリース日
2025年

## 修正内容

### バグ修正: ストリーク日数のリセット問題

#### 問題の詳細
ログインしていない日があったのに、ストリーク日数がリセットされない問題が発生していました。

**具体例:**
- プレイヤーが10日連続でログインして10日のストリークを達成
- その後3日間ログインしない（ログイン間隔が空く）
- 再度ログインしたとき、ストリークが1にリセットされるべきなのに、10のまま維持されていた

#### 原因
`EventListener.java` の `giveReward()` メソッド内のストリーク計算ロジックに問題がありました。
- 最終ストリーク日が「昨日」と一致する場合のみストリークを継続する処理はありましたが、
- 最終ストリーク日が「昨日でない」場合（ログイン間隔が空いた場合）にストリークをリセットする処理が欠けていました。

#### 修正内容
`EventListener.java` の `giveReward()` メソッドを以下のように修正しました:

```java
if (updateStreak && plugin.getConfig().getBoolean("streak-enabled", true)) {
    // ストリークを計算
    String lastStreakDateStr = plugin.getStorage().getLastStreakDate(playerId);
    if (lastStreakDateStr != null) {
        LocalDate lastStreakDate;
        // 日付時刻形式 "YYYY-MM-DD HH:mm" または日付のみ "YYYY-MM-DD" をサポート
        if (lastStreakDateStr.length() > 10) {
            // 日付時刻形式の場合、日付部分のみを抽出
            lastStreakDate = LocalDate.parse(lastStreakDateStr.substring(0, 10));
        } else {
            // 日付のみの形式
            lastStreakDate = LocalDate.parse(lastStreakDateStr);
        }
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (lastStreakDate.equals(yesterday)) {
            // 昨日ログインしていた場合、ストリークを継続
            streak = plugin.getStorage().getStreak(playerId) + 1;
        } else {
            // 昨日ログインしていない場合、ストリークをリセット ← 【追加】
            streak = 1;
        }
    } else {
        // 初回ログインの場合 ← 【追加】
        streak = 1;
    }
    plugin.getStorage().setStreak(playerId, streak);
    plugin.getStorage().setLastStreakDate(playerId, today);
}
```

**修正のポイント:**
1. 最終ログイン日が昨日**でない**場合、ストリークを1にリセット
2. 初回ログイン（最終ストリーク日が未設定）の場合も、ストリークを1に設定

#### 影響範囲
- ストリーク機能を有効にしているすべてのプレイヤー
- この修正により、ログイン間隔が空いた場合、正しくストリークが1にリセットされるようになります

## バージョン更新
- `plugin.yml`: version 1.3.3 → 1.3.4
- `build.gradle`: version 1.3.3 → 1.3.4

## アップグレード方法
1. サーバーを停止
2. `plugins/` フォルダ内の古い LoginBonus-1.3.3.jar を削除
3. 新しい LoginBonus-1.3.4.jar を `plugins/` フォルダに配置
4. サーバーを起動

既存のプレイヤーデータ（YAML/SQLite/MySQL）はそのまま使用できます。
設定ファイルの変更は不要です。

## 注意事項
- この修正により、既にストリークが不正に保持されているプレイヤーのデータは自動的には修正されません
- 管理者は必要に応じて `/rewardsetstreak <player> <正しい日数>` コマンドでストリークを手動調整できます

## 互換性
- Minecraft バージョン: 1.19.4
- Java バージョン: 17
- 前バージョン（1.3.3）からのアップグレード: 互換性あり
