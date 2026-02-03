# LoginBonus バージョン 1.4.0 変更点

## リリース日
2026年2月

## 更新内容

### 新機能: Planプラグイン連携

#### 概要
[Plan (Player Analytics)](https://github.com/plan-player-analytics/Plan)プラグインとの連携機能を追加しました。連続ログイン日数とランキングをPlanのWeb UIに表示できます。

#### 変更内容

1. **Plan DataExtension実装**
   - PlanのDataExtension APIを実装
   - プレイヤーの連続ログイン日数をPlanプロフィールに表示
   - サーバー全体の連続ログイン日数ランキング（上位50名）をPlanサーバーページに表示

2. **自動連携**
   - Planプラグインがインストールされている場合、自動的に連携
   - Planがない場合でも、LoginBonusは正常に動作

3. **依存関係の追加**
   - `build.gradle`: Plan API 5.6.3196を追加（compileOnly）
   - `plugin.yml`: Planをsoftdependとして追加

### バグ修正

#### 高優先度の修正

1. **NullPointerException対策**
   - `EventListener.java`の3箇所で`loginTimes.get(playerId)`がnullを返した場合の処理を追加
   - プレイヤーがログアウトした直後にタスクが実行された場合のクラッシュを防止
   - 修正箇所: `setupPlayerTracking`, `setupPlayerTrackingWithData`, `startTracking`メソッド

2. **設定値の検証強化**
   - `BarColor.valueOf()`と`BarStyle.valueOf()`にtry-catch追加
   - 無効な設定値が指定された場合、デフォルト値（BLUE, SOLID）を使用
   - 警告メッセージをログに出力

3. **アイテム設定の検証**
   - `giveItems`メソッドでitemNameとbaseAmountのnullチェックを追加
   - 設定が不正な場合、警告を出力してスキップ
   - プラグインのクラッシュを防止

## バージョン更新
- `plugin.yml`: version 1.3.7 → 1.4.0
- `build.gradle`: version 1.3.7 → 1.4.0

## 互換性

### サポート対象
- **Minecraftバージョン**: 1.21.8 (Spigot API 1.21.8-R0.1-SNAPSHOT)
- **Javaバージョン**: 17
- **Bukkit/Spigot/Paper**: 標準的なBukkit実装と互換
- **連携プラグイン**: Plan (Player Analytics) - オプション

### 以前のバージョンとの互換性
- **データ互換性**: 既存のプレイヤーデータ（YAML/SQLite/MySQL）はそのまま使用可能
- **設定互換性**: 既存の設定ファイル（config.yml、message.yml）の変更は不要
- **コマンド互換性**: すべてのコマンドとパーミッションは変更なし
- **API互換性**: 他のプラグインとのAPI互換性を維持

## アップグレード方法

### 通常のアップグレード
1. サーバーを停止
2. `plugins/` フォルダ内の古い LoginBonus JAR ファイルを削除
3. 新しい LoginBonus-1.4.0.jar を `plugins/` フォルダに配置
4. （オプション）Plan連携を利用する場合、Planプラグインをインストール
5. サーバーを起動

### 重要な注意事項
- プラグインデータ（playerdata.yml、playerdata.db、MySQLデータベース）はバックアップ推奨
- 設定ファイルの変更は不要です
- 既存の機能はすべて動作します
- Plan連携はオプションです（Planがなくても動作します）

## 動作確認済みの機能
- ログイン時間トラッキング
- 報酬システム
- ストリーク機能
- ボスバー表示
- データベース連携（YAML/SQLite/MySQL）
- 全コマンド（/rewardstreak、/rewardreload、etc.）
- 特別報酬システム（個別日数・倍数報酬）
- マルチサーバー同期（MySQL使用時）
- Plan連携（連続ログイン日数とランキング表示）

## 既知の問題
なし

## セキュリティ
- CodeQLスキャン実施：脆弱性なし
- 依存関係チェック：既知の脆弱性なし
- コードレビュー実施：問題なし

## 前バージョンからの変更点
- バージョン 1.3.7: Minecraft 1.21.8 対応
- バージョン 1.4.0: Plan連携追加、バグ修正

## 今後の予定
- 機能追加のフィードバック受付中
- バグ報告はGitHub Issuesへ

## 貢献者
- Plan連携実装: GitHub Copilot
- バグ修正: 自動コードレビューとCodeQL分析に基づく
