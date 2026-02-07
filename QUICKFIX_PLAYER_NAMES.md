# Quick Fix Guide: Player Names Not Displaying

## 問題 (Problem)
Bungeecord の Plan で、プレイヤー名が「abc12345...」のように UUID の断片で表示される。

## 原因 (Cause)
データベースに古いデータが残っており、`player_name` 列が空（NULL）のままになっている。

## 解決方法 (Solution)

### ステップ 1: プラグインを更新
1. 最新の LoginBonus.jar を Bukkit/Spigot サーバーにインストール
2. サーバーを再起動、またはプラグインをリロード

### ステップ 2: 移行コマンドを実行
Bukkit/Spigot サーバーのコンソールまたはゲーム内で：

```
/rewardmigratenames
```

### ステップ 3: 結果を確認
```
[LoginBonus] プレイヤー名の移行を開始します。対象: 150 人
[LoginBonus] プレイヤー名を更新: 550e8400-... -> Steve
[LoginBonus] プレイヤー名を更新: 6ba7b810-... -> Alice
...
[LoginBonus] プレイヤー名の移行が完了しました。更新数: 147 人
```

### ステップ 4: Plan で確認
1. 数分待つ（キャッシュのクリア）
2. Plan のネットワークページにアクセス
3. LoginBonus のランキングをチェック
4. プレイヤー名が正しく表示されることを確認 ✅

---

## 重要な注意事項

### ✅ できること
- 空の player_name を自動的に埋める
- 何度実行しても安全（冪等性）
- オフラインのプレイヤーの名前も取得可能

### ❌ できないこと
- 一度もサーバーに参加していないプレイヤーの名前は取得不可
- YAML/SQLite ストレージでは実行不可（MySQL のみ）
- Bungeecord サーバーでは実行不可（Bukkit サーバーで実行）

### 🔒 権限
- OP 権限または `loginbonus.admin` 権限が必要

---

## トラブルシューティング

### Q: 「このコマンドは MySQL ストレージを使用している場合のみ実行できます」と表示される
**A**: `config.yml` で `storage-type: mysql` に設定されているか確認してください。

### Q: 一部のプレイヤー名が更新されない
**A**: そのプレイヤーが一度もサーバーに参加していない可能性があります。参加後に名前が表示されます。

### Q: Bungeecord で実行しようとしたがエラーになる
**A**: このコマンドは Bukkit/Spigot サーバーで実行する必要があります。

### Q: 実行後も Plan で名前が表示されない
**A**: 
1. Plan のキャッシュがクリアされるまで数分待つ
2. ブラウザのキャッシュをクリア（Ctrl+F5）
3. Plan のプラグインを再起動

---

## 今後について

### 新しいプレイヤー
自動的に名前が保存されるため、何もする必要はありません。

### 既存のプレイヤー
移行コマンドを実行した後は、次回ログイン時に自動的に名前が更新されます。

---

## コマンド概要

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/rewardmigratenames` | 空のプレイヤー名を移行 | `loginbonus.admin` |

---

## さらに詳しい情報

技術的な詳細については、`FIX_PLAYER_NAME_MIGRATION.md` を参照してください。

---

# English Quick Guide

## Problem
Player names display as UUID fragments (e.g., "abc12345...") on Bungeecord Plan.

## Cause
Old database entries have NULL player_name values.

## Solution

### Step 1: Update Plugin
Install the latest LoginBonus.jar on your Bukkit/Spigot server and restart.

### Step 2: Run Migration Command
On Bukkit/Spigot server console or in-game:
```
/rewardmigratenames
```

### Step 3: Verify Results
Check Plan's network page - player names should now display correctly! ✅

### Requirements
- OP or `loginbonus.admin` permission
- MySQL storage enabled
- Run on Bukkit/Spigot server (not Bungeecord)

---

**That's it! プレイヤー名が正しく表示されるようになります！** 🎉
