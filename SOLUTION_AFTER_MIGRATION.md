# 解決策: プレイヤー名が移行後も表示されない問題

## 問題の報告
ユーザーからの報告:
> `/rewardmigratenamesを行い、念の為にBungeecordを再起動しましたが、まだ正常にプレイヤー名が反映されていません`

## 実装した解決策

### 1. デバッグコマンドの追加 🔍

**新コマンド: `/loginbonusdebug`**

Bungeecordサーバーで実行できる診断コマンド。

**機能:**
- データベース内のプレイヤー数を表示
- 上位10名のプレイヤー情報を表示（名前の有無を明示）
- 統計情報を表示（名前あり/なしの数）
- 問題に応じた推奨アクションを表示

**使用例:**
```
/loginbonusdebug
```

**出力例（問題がある場合）:**
```
=== LoginBonus Debug Info ===
データベース内のプレイヤー数: 150
=== 上位10名のプレイヤー情報 ===
✗ [名前なし] (UUID: 550e8400..., Streak: 10)
✗ [名前なし] (UUID: 6ba7b810..., Streak: 5)
...
=== 全体統計（サンプル10名） ===
名前あり: 2
名前なし: 8

推奨アクション:
1. Bukkitサーバーで /rewardmigratenames を実行してください
```

**出力例（データは正常な場合）:**
```
=== LoginBonus Debug Info ===
データベース内のプレイヤー数: 150
=== 上位10名のプレイヤー情報 ===
✓ Steve (UUID: 550e8400..., Streak: 10)
✓ Alice (UUID: 6ba7b810..., Streak: 5)
...
=== 全体統計（サンプル10名） ===
名前あり: 10
名前なし: 0

✓ 全てのプレイヤーに名前が設定されています！
まだPlanで名前が表示されない場合:
1. Planのキャッシュをクリアしてください
2. Bungeecordを再起動してください
3. ブラウザのキャッシュをクリアしてください (Ctrl+Shift+R)
```

### 2. Plan統合の改善 📊

**変更内容:**

#### A. より頻繁なデータ更新
```java
// 以前
CallEvents.SERVER_PERIODICAL  // 定期的にのみ更新

// 現在
CallEvents.SERVER_PERIODICAL  // 定期的
CallEvents.PLAYER_JOIN        // プレイヤー参加時
CallEvents.PLAYER_LEAVE       // プレイヤー退出時
```

**効果:**
- プレイヤーがログイン/ログアウトするたびにPlanが更新される
- より最新のデータが表示される
- キャッシュの問題が軽減される

#### B. テーブル名の明示
```java
// 以前
@TableProvider(tableColor = Color.LIGHT_GREEN)

// 現在
@TableProvider(tableColor = Color.LIGHT_GREEN, tableName = "consecutive_login_ranking")
```

**効果:**
- Planのキャッシュ管理が改善される
- テーブルデータの識別が明確になる

### 3. デバッグログの追加 📝

**BungeeMySqlReader.getPlayerName()に追加:**
- player_nameがNULLまたは空の場合にログ出力
- 問題の早期発見が可能

### 4. 包括的なドキュメント 📚

**3つの新しいドキュメント:**

1. **TROUBLESHOOTING_PLAYER_NAMES.md**
   - 技術的な詳細
   - すべての可能な原因と解決方法
   - データベースクエリ例
   - 上級ユーザー向け

2. **EMERGENCY_FIX_GUIDE.md**
   - 即座に試すべき手順
   - 結果に応じた対応
   - チェックリスト形式
   - 一般ユーザー向け

3. 既存ドキュメントの更新
   - QUICKFIX_PLAYER_NAMES.md
   - COMPLETE_SOLUTION_SUMMARY.md

## あなたがすべきこと

### 最優先: デバッグコマンドで診断

1. **最新のプラグインをインストール**
   - LoginBonus v1.5.0（最新版）
   - BukkitとBungeecordの両方

2. **Bungeecordサーバーで実行:**
   ```
   /loginbonusdebug
   ```

3. **結果を確認:**

   **ケースA: 「名前なし」が多い**
   ```
   名前なし: 8
   ```
   → データベースに名前がない
   → **対応:** Bukkitで `/rewardmigratenames` を再実行

   **ケースB: 「名前あり」が多い**
   ```
   名前あり: 10
   名前なし: 0
   ✓ 全てのプレイヤーに名前が設定されています！
   ```
   → データは正常、Planのキャッシュ問題
   → **対応:** 下記の「Planキャッシュ問題の解決」を実行

### Planキャッシュ問題の解決

データベースに名前があるのに表示されない場合:

1. **Bungeecordを完全再起動**
   ```
   /end
   ```
   → 起動を待つ

2. **ブラウザのキャッシュをクリア**
   - **Chrome/Edge**: Ctrl + Shift + Delete
   - **強制リロード**: Ctrl + Shift + R

3. **新しいタブで開く**
   - または
   - シークレットモード/プライベートウィンドウで開く

4. **10分待つ**
   - Planの更新サイクルを待つ
   - この間にプレイヤーがログイン/ログアウトすると更新が促進される

### データベース設定の確認

両方のサーバーで同じデータベースを使用していることを確認:

**Bukkit側 config.yml:**
```yaml
storage-type: mysql
mysql:
  host: localhost
  port: 3306
  database: loginbonus
  table-name: player_data
  username: root
  password: password
```

**Bungeecord側 config.yml:**
```yaml
mysql:
  host: localhost        # ← Bukkitと同じ
  port: 3306            # ← Bukkitと同じ
  database: loginbonus  # ← Bukkitと同じ
  table-name: player_data  # ← Bukkitと同じ
  username: root
  password: password
```

## 問題が解決する理由

### これまでの問題:
1. ❌ Planが古いデータをキャッシュ
2. ❌ 更新頻度が低い（定期的のみ）
3. ❌ 問題の診断が困難
4. ❌ 何が悪いのか不明

### 今回の改善:
1. ✅ より頻繁な更新（参加/退出時）
2. ✅ デバッグコマンドで即座に診断可能
3. ✅ 明確な推奨アクション
4. ✅ 詳細なドキュメント

## 技術的な詳細

### Plan DataExtensionの更新ロジック

**以前:**
```
定期的（5-10分ごと）に更新
→ 最大10分間古いデータが表示される可能性
```

**現在:**
```
1. 定期的に更新（5-10分ごと）
2. プレイヤー参加時に更新
3. プレイヤー退出時に更新
→ リアルタイムに近い更新
```

### キャッシュ階層

プレイヤー名表示には複数のキャッシュが関与:

```
ブラウザキャッシュ
    ↓
Planのキャッシュ
    ↓
Bungeecordのメモリ
    ↓
MySQLデータベース
```

すべての層で最新データが必要です。

## まとめ

### 今すぐやるべきこと:

1. **診断**
   ```
   /loginbonusdebug
   ```

2. **結果に応じた対応**
   - 名前なし多い → Bukkitで移行再実行
   - 名前あり多い → キャッシュクリア

3. **確認**
   - Bungeecord再起動
   - ブラウザキャッシュクリア
   - 10分待つ

### 改善された点:

- ✅ デバッグコマンド追加
- ✅ より頻繁なデータ更新
- ✅ 明確なテーブル識別
- ✅ 詳細なドキュメント
- ✅ 推奨アクションの自動表示

### サポートが必要な場合:

1. `/loginbonusdebug` の出力を保存
2. Bukkitの `/rewardmigratenames` の出力を保存
3. 両方の `config.yml` を確認
4. データベースクエリ結果を取得
5. これらの情報とともにサポートに連絡

---

**この更新で、ほとんどの問題は `/loginbonusdebug` で診断可能になり、明確な解決手順が提示されます！** 🎉
