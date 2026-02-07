# 緊急対応ガイド: プレイヤー名が表示されない問題

## 現在の状況
`/rewardmigratenames`を実行し、Bungeecordを再起動したが、まだプレイヤー名が表示されない。

## 即座に試すべき手順

### ステップ 1: デバッグコマンドで診断（必須）

**Bungeecordサーバーで実行:**
```
/loginbonusdebug
```

このコマンドで問題の原因が特定できます。

---

### ステップ 2: 結果に応じた対応

#### パターンA: 「名前なし」が多い場合

**デバッグコマンドの出力例:**
```
✗ [名前なし] (UUID: 550e8400..., Streak: 10)
✗ [名前なし] (UUID: 6ba7b810..., Streak: 5)
...
名前なし: 8
```

**対応:**
1. **Bukkitサーバー**で再度実行:
   ```
   /rewardmigratenames
   ```

2. 出力を確認:
   ```
   更新数: X 人  ← この数字を確認
   ```

3. 「更新数: 0 人」の場合:
   - プレイヤーが一度もサーバーに参加していない
   - プレイヤーに再ログインしてもらう必要がある

4. 「更新数: X 人」（X > 0）の場合:
   - Bungeecordを再起動
   - `/loginbonusdebug`で再確認

---

#### パターンB: 「名前あり」が多いのに表示されない場合

**デバッグコマンドの出力例:**
```
✓ Steve (UUID: 550e8400..., Streak: 10)
✓ Alice (UUID: 6ba7b810..., Streak: 5)
...
名前あり: 10
名前なし: 0
✓ 全てのプレイヤーに名前が設定されています！
```

これは**Planのキャッシュ問題**です。以下を順番に実行:

**A. Bungeecordを再起動**
```
/end
```
→ 起動後、5分待つ

**B. ブラウザのキャッシュをクリア**
- **Chrome/Edge**: Ctrl + Shift + Delete → 「キャッシュされた画像とファイル」をチェック → クリア
- **強制リロード**: Ctrl + Shift + R （または Ctrl + F5）

**C. Planのページを開き直す**
- 新しいタブで開く
- シークレットモードで開く

**D. 10分待つ**
- Planは定期的にデータを更新します
- 更新サイクルを待つ必要がある場合があります

---

### ステップ 3: データベースを直接確認（上級者向け）

MySQLに接続して確認:
```sql
-- 名前が設定されているか確認
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN player_name IS NULL OR player_name = '' THEN 1 ELSE 0 END) as without_name,
    SUM(CASE WHEN player_name IS NOT NULL AND player_name != '' THEN 1 ELSE 0 END) as with_name
FROM player_data;
```

**期待される結果:**
```
total | without_name | with_name
------|--------------|----------
150   | 0            | 150
```

**問題がある場合:**
```
total | without_name | with_name
------|--------------|----------
150   | 147          | 3
```
→ 移行が完了していない → Bukkitで`/rewardmigratenames`を再実行

---

## よくある間違い

### ❌ 間違い 1: Bungeecordで移行コマンドを実行
```
# Bungeecordサーバーで実行 ← これは間違い！
/rewardmigratenames
```
**正しい方法:** Bukkitサーバーで実行する必要があります

### ❌ 間違い 2: データベース設定が異なる
- BukkitとBungeecordで別々のデータベースを使用している
- テーブル名が異なる
- データベース名が異なる

**確認方法:**
```yaml
# Bukkit側 config.yml
mysql:
  database: loginbonus
  table-name: player_data

# Bungeecord側 config.yml
mysql:
  database: loginbonus  ← 同じである必要がある
  table-name: player_data  ← 同じである必要がある
```

### ❌ 間違い 3: Planだけを再起動
- Planを再起動しても、Bungeecordが古いデータを保持している可能性がある

**正しい方法:** Bungeecord全体を再起動

---

## チェックリスト

以下を順番に確認してください:

- [ ] 1. `/loginbonusdebug`をBungeecordで実行して結果を確認
- [ ] 2. 「名前なし」が多い場合: Bukkitで`/rewardmigratenames`を再実行
- [ ] 3. Bungeecordを完全に再起動（/end → 起動）
- [ ] 4. ブラウザのキャッシュをクリア（Ctrl+Shift+R）
- [ ] 5. 新しいタブまたはシークレットモードでPlanを開く
- [ ] 6. 5-10分待つ（Planの更新サイクル）
- [ ] 7. 問題が続く場合: データベース設定を確認
- [ ] 8. それでもダメな場合: プレイヤーに再ログインしてもらう

---

## それでも解決しない場合

以下の情報を収集してください:

1. **`/loginbonusdebug`の完全な出力**

2. **Bukkitの`/rewardmigratenames`の出力:**
   ```
   [LoginBonus] プレイヤー名の移行を開始します。対象: X 人
   [LoginBonus] プレイヤー名の移行が完了しました。更新数: Y 人
   ```

3. **データベースクエリの結果:**
   ```sql
   SELECT uuid, player_name, streak FROM player_data LIMIT 10;
   ```

4. **設定ファイル:**
   - Bukkit側の`config.yml`（MySQLセクション）
   - Bungeecord側の`config.yml`（MySQLセクション）

5. **バージョン情報:**
   - LoginBonusのバージョン（1.5.0）
   - Planのバージョン
   - Bungeecordのバージョン

---

## 最も可能性が高い原因

経験上、以下のいずれかです:

### 🥇 1位: Planのキャッシュ（50%）
**解決方法:** Bungeecord再起動 + ブラウザキャッシュクリア + 10分待つ

### 🥈 2位: データベース設定の不一致（30%）
**解決方法:** Bukkit/Bungeecordの両方の`config.yml`を確認

### 🥉 3位: プレイヤーが一度も参加していない（15%）
**解決方法:** プレイヤーに再ログインしてもらう

### その他（5%）
**解決方法:** データベース直接確認 + サポートに連絡

---

## 今回追加された改善点

### v1.5.0での改善:
1. ✅ より頻繁なデータ更新（プレイヤー参加/退出時）
2. ✅ デバッグコマンド追加（`/loginbonusdebug`）
3. ✅ 詳細なトラブルシューティングガイド

これらの改善により、問題の診断と解決が大幅に簡単になりました！

---

## まとめ

**最も重要な3つのステップ:**
1. 📊 `/loginbonusdebug` で診断
2. 🔄 必要に応じてBukkitで `/rewardmigratenames` 再実行
3. 🔄 Bungeecord再起動 + ブラウザキャッシュクリア

**これで99%の問題は解決します！** 🎉
