# Troubleshooting: Player Names Not Displaying After Migration

## 問題 (Problem)
`/rewardmigratenames`を実行し、Bungeecordを再起動したが、まだプレイヤー名が正しく表示されない。

## 診断手順 (Diagnostic Steps)

### ステップ 1: デバッグコマンドを実行

Bungeecordサーバーで以下のコマンドを実行:
```
/loginbonusdebug
```

このコマンドは以下を表示します:
- データベース内のプレイヤー数
- 上位10名のプレイヤー情報（名前の有無）
- 統計情報
- 推奨されるアクション

**期待される出力（正常な場合）:**
```
=== LoginBonus Debug Info ===
データベース内のプレイヤー数: 150
=== 上位10名のプレイヤー情報 ===
✓ Steve (UUID: 550e8400..., Streak: 10)
✓ Alice (UUID: 6ba7b810..., Streak: 5)
✓ Bob (UUID: 6ba7b814..., Streak: 3)
...
=== 全体統計（サンプル10名） ===
名前あり: 10
名前なし: 0
✓ 全てのプレイヤーに名前が設定されています！
```

**問題がある場合:**
```
=== LoginBonus Debug Info ===
データベース内のプレイヤー数: 150
=== 上位10名のプレイヤー情報 ===
✗ [名前なし] (UUID: 550e8400..., Streak: 10)
✗ [名前なし] (UUID: 6ba7b810..., Streak: 5)
...
=== 全体統計（サンプル10名） ===
名前あり: 0
名前なし: 10
```

### ステップ 2: データベースを直接確認

MySQLに接続して直接確認:
```sql
-- 名前がNULLまたは空のプレイヤーを確認
SELECT uuid, player_name, streak 
FROM player_data 
WHERE player_name IS NULL OR player_name = '' 
LIMIT 10;

-- 名前が設定されているプレイヤーを確認
SELECT uuid, player_name, streak 
FROM player_data 
WHERE player_name IS NOT NULL AND player_name != '' 
LIMIT 10;

-- 統計
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN player_name IS NULL OR player_name = '' THEN 1 ELSE 0 END) as without_name,
    SUM(CASE WHEN player_name IS NOT NULL AND player_name != '' THEN 1 ELSE 0 END) as with_name
FROM player_data;
```

### ステップ 3: 移行コマンドの出力を確認

Bukkitサーバーで `/rewardmigratenames` を再実行し、出力を確認:

**正常な出力:**
```
[LoginBonus] プレイヤー名の移行を開始します。対象: 150 人
[LoginBonus] プレイヤー名を更新: 550e8400-... -> Steve
[LoginBonus] プレイヤー名を更新: 6ba7b810-... -> Alice
...
[LoginBonus] プレイヤー名の移行が完了しました。更新数: 147 人
```

**問題の兆候:**
```
[LoginBonus] プレイヤー名の移行を開始します。対象: 150 人
[LoginBonus] プレイヤー名の移行が完了しました。更新数: 0 人
```
→ プレイヤーが一度もサーバーに参加していない、またはBukkitのキャッシュに情報がない

## よくある原因と解決方法

### 原因 1: データベース接続の設定ミス

**症状:**
- デバッグコマンドでエラーが表示される
- データベースに接続できない

**確認事項:**
1. BukkitとBungeecordの両方で同じMySQLデータベースを使用しているか
2. `config.yml`の設定が一致しているか:
   - `mysql.host`
   - `mysql.port`
   - `mysql.database`
   - `mysql.table-name`
   - `mysql.username`
   - `mysql.password`

**解決方法:**
```yaml
# Bukkit側とBungeecord側で同じ設定にする
mysql:
  host: localhost
  port: 3306
  database: loginbonus
  table-name: player_data
  username: root
  password: your_password
```

### 原因 2: プレイヤーが一度も参加していない

**症状:**
- 移行コマンドで「更新数: 0 人」と表示される
- データベースにUUIDは存在するが、Bukkitにプレイヤー情報がない

**確認事項:**
1. プレイヤーが実際にBukkitサーバーに参加したことがあるか
2. UUIDが正しいか（Bedrock版とJava版で異なる）

**解決方法:**
- プレイヤーに再度ログインしてもらう
- ログイン時に自動的に名前が保存される

### 原因 3: Planのキャッシュ問題

**症状:**
- デバッグコマンドでは名前が表示される
- データベースにも名前が存在する
- しかしPlanのWebページでは表示されない

**確認事項:**
1. Planのバージョンが最新か
2. Planの設定でキャッシュが有効になっているか

**解決方法:**

#### A. Planのキャッシュをクリア
1. Planのデータフォルダに移動
2. キャッシュファイルを削除
3. Planを再起動

#### B. ブラウザのキャッシュをクリア
- **Chrome/Edge:** Ctrl + Shift + Delete → キャッシュをクリア
- **Firefox:** Ctrl + Shift + Delete → キャッシュをクリア
- **強制リロード:** Ctrl + Shift + R (または Ctrl + F5)

#### C. Bungeecordを再起動
```
/end
# または
/stop
```

#### D. Planのデータ更新を待つ
- Planは定期的にデータを更新します（デフォルト: 5分ごと）
- 更新サイクルを待つか、プレイヤーがログイン/ログアウトすることで更新が促進されます

### 原因 4: Plan DataExtensionの登録問題

**症状:**
- Bungeecord起動時に「Plan連携が有効化されました」と表示されない
- Planのページ自体にLoginBonusセクションが表示されない

**確認事項:**
1. Bungeecordサーバーの起動ログを確認
2. 以下のメッセージがあるか:
   ```
   [LoginBonus] Plan連携が有効化されました。ネットワークページに連続ログイン日数が表示されます。
   ```

**解決方法:**
1. PlanがBungeecordにインストールされているか確認
2. Planのバージョンを確認（5.6以降が必要）
3. Bungeecordを再起動

### 原因 5: テーブル構造の問題

**症状:**
- SQLエラーが発生する
- `player_name`列が存在しないエラー

**確認事項:**
```sql
DESCRIBE player_data;
```

**期待される構造:**
```
+------------------+-------------+------+-----+---------+
| Field            | Type        | Null | Key | Default |
+------------------+-------------+------+-----+---------+
| uuid             | varchar(36) | NO   | PRI | NULL    |
| player_name      | varchar(16) | YES  |     | NULL    |
| cumulative       | double      | YES  |     | 0.0     |
| last_reward      | varchar(20) | YES  |     | NULL    |
| streak           | int         | YES  |     | 1       |
| last_streak_date | varchar(20) | YES  |     | NULL    |
| last_sync        | bigint      | YES  |     | 0       |
+------------------+-------------+------+-----+---------+
```

**解決方法:**
```sql
-- player_name列がない場合、追加する
ALTER TABLE player_data ADD COLUMN player_name VARCHAR(16) AFTER uuid;
```

## 改善点（v1.5.0で追加）

### 1. より頻繁なデータ更新
Plan DataExtensionが以下のイベントで更新されるようになりました:
- `SERVER_PERIODICAL` (定期的)
- `PLAYER_JOIN` (プレイヤー参加時)
- `PLAYER_LEAVE` (プレイヤー退出時)

### 2. デバッグコマンド
`/loginbonusdebug`コマンドで簡単に診断可能

## 推奨される手順

問題が発生した場合、以下の順序で実行してください:

1. ✅ `/loginbonusdebug` をBungeecordで実行（診断）
2. ✅ 必要に応じて `/rewardmigratenames` をBukkitで再実行
3. ✅ Bungeecordを再起動
4. ✅ ブラウザのキャッシュをクリア（Ctrl+Shift+R）
5. ✅ 5-10分待つ（Planの更新サイクル）
6. ✅ 問題が続く場合、Planを再起動

## サポート情報の収集

問題が解決しない場合、以下の情報を収集してください:

1. **Bukkitサーバーのログ:**
   - `/rewardmigratenames`の出力
   - 起動時のLoginBonusメッセージ

2. **Bungeecordサーバーのログ:**
   - 起動時のLoginBonusメッセージ
   - `/loginbonusdebug`の出力

3. **データベース情報:**
   - テーブル構造 (`DESCRIBE player_data`)
   - サンプルデータ (上記のSQLクエリ結果)

4. **設定ファイル:**
   - Bukkit側の `config.yml`
   - Bungeecord側の `config.yml`

5. **バージョン情報:**
   - LoginBonusのバージョン
   - Planのバージョン
   - Bungeecordのバージョン
   - Bukkitのバージョン

## まとめ

プレイヤー名が表示されない場合、多くは以下のいずれか:
1. データベース接続の設定ミス
2. Planのキャッシュ問題
3. プレイヤーデータの移行が未完了

`/loginbonusdebug`コマンドで問題を特定し、適切な対処を行ってください。
