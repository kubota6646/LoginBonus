# LoginBonus バージョン 1.3.5 変更点

## リリース日
2025年

## 修正内容

### バグ修正1: ストリーク日数のリセット問題

#### 問題の詳細
ログインしていない日があったのに、ストリーク日数がリセットされない問題が発生していました。

**具体例:**
- プレイヤーが10日連続でログインして10日のストリークを達成
- その後3日間ログインしない（ログイン間隔が空く）
- 再度ログインしたとき、ストリークが1にリセットされるべきなのに、10のまま維持されていた

#### 根本原因
`EventListener.java` の `giveReward()` メソッド内のストリーク計算ロジックに2つの問題がありました：

1. **初回の修正で対応した問題**: 最終ストリーク日が「昨日でない」場合（ログイン間隔が空いた場合）にストリークをリセットする処理が欠けていました。

2. **追加で発見された問題**: リセット時刻を考慮した日付比較ができていませんでした。
   - システムは「リセット時刻」を考慮した独自の日付概念を使用（例：リセット時刻が06:00の場合、04:00時点ではまだ前日扱い）
   - しかし、比較には `LocalDate.now()` を使用していたため、リセット時刻を無視した通常の暦日で比較していました
   - これにより、リセット時刻が00:00以外の場合に誤った判定が行われていました

**問題の例：**
- リセット時刻: 06:00
- 12月20日 08:00にログイン → リセット日付 "2025-12-20 06:00" として保存
- 12月21日 04:00にログイン（リセット前） → リセット日付はまだ "2025-12-20 06:00"
- しかし `LocalDate.now()` は "2025-12-21" を返す
- `yesterday` は "2025-12-20" と計算される
- 保存されたリセット日付 "2025-12-20" と `yesterday` "2025-12-20" が一致
- 結果：ストリーク継続（正しくは同じ日なのでまだ報酬を受け取れないはず）

#### 修正内容
`EventListener.java` の `giveReward()` メソッドを以下のように修正しました：

```java
if (updateStreak && plugin.getConfig().getBoolean("streak-enabled", true)) {
    String lastStreakDateStr = plugin.getStorage().getLastStreakDate(playerId);
    boolean shouldContinueStreak = false;
    
    if (lastStreakDateStr != null && !lastStreakDateStr.isEmpty()) {
        // 現在のリセット日付から日付部分を取得
        LocalDate currentResetDate;
        if (today.length() > 10) {
            currentResetDate = LocalDate.parse(today.substring(0, 10));
        } else {
            currentResetDate = LocalDate.parse(today);
        }
        
        // 最後のストリーク日付から日付部分を取得
        LocalDate lastStreakDate;
        if (lastStreakDateStr.length() > 10) {
            lastStreakDate = LocalDate.parse(lastStreakDateStr.substring(0, 10));
        } else {
            lastStreakDate = LocalDate.parse(lastStreakDateStr);
        }
        
        // 前回のリセット日付を計算（currentResetDateの1日前）
        LocalDate previousResetDate = currentResetDate.minusDays(1);
        
        // 最後のストリーク日付が前回のリセット日付と一致する場合、ストリークを継続
        shouldContinueStreak = lastStreakDate.equals(previousResetDate);
    }
    
    if (shouldContinueStreak) {
        // 前回のリセット日にログインしていた場合、ストリークを継続
        streak = plugin.getStorage().getStreak(playerId) + 1;
    } else {
        // 前回のリセット日にログインしていない場合、または初回ログインの場合、ストリークをリセット
        streak = 1;
    }
    
    plugin.getStorage().setStreak(playerId, streak);
    plugin.getStorage().setLastStreakDate(playerId, today);
}
```

**修正のポイント:**
1. 最終ログイン日が前回のリセット日**でない**場合、ストリークを1にリセット
2. 初回ログイン（最終ストリーク日が未設定）の場合も、ストリークを1に設定
3. **重要**: `LocalDate.now()` を使用せず、`today` パラメータ（リセット時刻を考慮した日付）から日付部分を抽出して比較するように変更
4. リセット時刻が00:00以外でも正しく動作するように修正

#### 影響範囲
- ストリーク機能を有効にしているすべてのプレイヤー
- 特にリセット時刻を00:00以外に設定している環境で大きく改善されます
- この修正により、ログイン間隔が空いた場合、正しくストリークが1にリセットされるようになります

### バグ修正2: 強制付与コマンド後の再ログイン問題

#### 問題の詳細
`/rewardforcegive` コマンドを使用して報酬を強制付与した後、プレイヤーが再ログインすると、報酬のカウントが再開されてしまう問題が発生していました。

**具体例:**
- 管理者が `/rewardforcegive PlayerName` を実行して報酬を強制付与
- プレイヤーがログアウト後、再ログイン
- システムがまだ報酬を受け取っていないと判断し、カウントが再開される

#### 根本原因
`RewardForceGiveCommand.java` で日付の形式が不一致でした：
- 強制付与コマンド: `LocalDate.now().toString()` を使用 → "2025-12-21" という形式で保存
- トラッキングシステム: `plugin.getResetDate()` を使用 → "2025-12-21 06:00" という形式（リセット時刻を含む）

この形式の違いにより、再ログイン時に `lastReward` との比較が一致せず、システムが報酬未受取と誤認識していました。

#### 修正内容
`RewardForceGiveCommand.java` を修正し、`plugin.getResetDate()` を使用するように変更しました：

```java
// 修正前
String today = LocalDate.now().toString();

// 修正後
String today = plugin.getResetDate();
```

**修正のポイント:**
- 強制付与コマンドでもリセット時刻を考慮した日付形式を使用
- トラッキングシステムと同じ形式で `lastReward` を保存
- これにより、再ログイン時に正しく「今日は既に報酬受取済み」と認識される

#### 影響範囲
- `/rewardforcegive` コマンドを使用する管理者
- この修正により、強制付与後の再ログインで正しく報酬受取済みとして扱われます

## バージョン更新
- `plugin.yml`: version 1.3.3 → 1.3.5
- `build.gradle`: version 1.3.3 → 1.3.5

## アップグレード方法
1. サーバーを停止
2. `plugins/` フォルダ内の古い LoginBonus JAR ファイルを削除
3. 新しい LoginBonus-1.3.5.jar を `plugins/` フォルダに配置
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
