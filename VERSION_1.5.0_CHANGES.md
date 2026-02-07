# LoginBonus バージョン 1.5.0 変更点

## 新機能

### Bungeecord対応
- **Bungeecord版プラグインを追加**: LoginBonusがBungeecordサーバーにもインストール可能になりました。
- **Planネットワークページ統合**: Bungeecord版をインストールすることで、Planのネットワークページに連続ログイン日数のランキングが表示されます。
- **ネットワーク全体の統計**: 複数のSpigot/Bukkitサーバーから収集されたデータを、Bungeecordプラグインが一元的に表示します。

## 技術的な変更点

### Bungeecord プラグイン
- `BungeeMain`: Bungeecord版のメインクラス
- `BungeeMySqlReader`: MySQLデータベースから連続ログイン日数を読み取るクラス
- `BungeePlanExtension`: Planのネットワークページにデータを提供するクラス
- `BungeePlanHook`: Plan連携を管理するクラス

### ビルド設定
- **Minecraft Librariesリポジトリを追加**: Bungeecord APIの依存関係（brigadier）を解決するため、`https://libraries.minecraft.net/` を追加しました

### 設定ファイル
- `bungee.yml`: Bungeecord版プラグインの基本情報
- `bungee-config.yml`: Bungeecord版プラグインの設定ファイルテンプレート

## インストール方法

### Spigot/Bukkitサーバー（既存）
1. `LoginBonus.jar`を`plugins/`フォルダに配置
2. MySQLを使用する場合、`config.yml`でMySQL設定を構成

### Bungeecordサーバー（新規）
1. `LoginBonus.jar`を Bungeecord の `plugins/`フォルダに配置
2. `plugins/LoginBonus/config.yml`を編集し、Spigotサーバーと同じMySQL接続情報を設定
3. Bungeecord に Plan プラグインがインストールされていることを確認

## 使用方法

### 前提条件
- MySQL データベースが必要です（Spigot サーバーと Bungeecord で共有）
- Plan プラグインが Bungeecord にインストールされている必要があります

### 設定例

#### Spigot/Bukkit サーバーの config.yml
```yaml
storage-type: mysql
mysql:
  host: localhost
  port: 3306
  database: loginbonus
  table-name: player_data
  username: root
  password: your_password
```

#### Bungeecord サーバーの config.yml
```yaml
mysql:
  host: localhost
  port: 3306
  database: loginbonus
  table-name: player_data
  username: root
  password: your_password
```

### 動作の仕組み
1. プレイヤーが Spigot/Bukkit サーバーでログインすると、連続ログイン日数が MySQL に保存されます
2. Bungeecord版プラグインが定期的に MySQL からデータを読み取ります
3. Plan のネットワークページに、全サーバーの連続ログイン日数ランキングが表示されます

## 注意事項
- Bungeecord版は読み取り専用です。データの更新は Spigot/Bukkit サーバーで行われます
- Bungeecord版を使用するには、必ず MySQL ストレージを使用する必要があります
- Plan プラグインがインストールされていない場合、Bungeecord版は警告を表示しますが、正常に動作します

## 互換性
- Bungeecord 1.20以降
- Plan 5.6以降
- MySQL 5.7以降または MariaDB 10.3以降
