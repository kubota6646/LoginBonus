# LoginBonus バージョン 1.3.6 変更点

## リリース日
2026年2月

## 更新内容

### Minecraft 1.21.11 対応

#### 概要
プラグインをMinecraft 1.21.11に対応させました。

#### 変更内容

1. **Spigot API の更新**
   - `build.gradle` の依存関係を更新
   - 変更前: `org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT`
   - 変更後: `org.spigotmc:spigot-api:1.21.11-R0.2-SNAPSHOT`

2. **API バージョンの更新**
   - `plugin.yml` のapi-versionを更新
   - 変更前: `api-version: 1.19`
   - 変更後: `api-version: 1.21`

3. **ドキュメントの更新**
   - `README.md` の互換性セクションを更新
   - Minecraft 1.21.11 対応を明記

#### 技術的な詳細
プラグインは標準的なBukkit API（BossBar、BarColor、BarStyle、ChatColorなど）を使用しているため、Javaコードの変更は不要でした。これらのAPIはMinecraftバージョン間で安定しており、互換性が保たれています。

## バージョン更新
- `plugin.yml`: version 1.3.5 → 1.3.6
- `build.gradle`: version 1.3.5 → 1.3.6

## 互換性

### サポート対象
- **Minecraftバージョン**: 1.21.11 (Spigot API 1.21.11-R0.2-SNAPSHOT)
- **Javaバージョン**: 17
- **Bukkit/Spigot/Paper**: 標準的なBukkit実装と互換

### 以前のバージョンとの互換性
- **データ互換性**: 既存のプレイヤーデータ（YAML/SQLite/MySQL）はそのまま使用可能
- **設定互換性**: 既存の設定ファイル（config.yml、message.yml）の変更は不要
- **コマンド互換性**: すべてのコマンドとパーミッションは変更なし

## アップグレード方法

### 通常のアップグレード
1. サーバーを停止
2. `plugins/` フォルダ内の古い LoginBonus JAR ファイルを削除
3. 新しい LoginBonus-1.3.6.jar を `plugins/` フォルダに配置
4. サーバーを起動

### 重要な注意事項
- プラグインデータ（playerdata.yml、playerdata.db、MySQLデータベース）はバックアップ推奨
- 設定ファイルの変更は不要です
- 既存の機能はすべて動作します

## 動作確認済みの機能
- ログイン時間トラッキング
- 報酬システム
- ストリーク機能
- ボスバー表示
- データベース連携（YAML/SQLite/MySQL）
- 全コマンド（/rewardstreak、/rewardreload、etc.）
- 特別報酬システム（個別日数・倍数報酬）
- マルチサーバー同期（MySQL使用時）

## 既知の問題
なし

## 今後の予定
- 機能追加のフィードバック受付中
- バグ報告はGitHub Issuesへ
