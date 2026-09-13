---
feature: ci-and-repo-automation
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# 実装タスク計画書: CI およびリポジトリ自動化（CI & Repository Automation）

## 1. スタック PR 概要（Executive Stacked PR Overview）

| PR # | ターゲットブランチ | フェーズ / 目的 | 主要コンポーネント | 依存関係 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/ci-phase1-dev-orchestration-and-governance` | 開発者ツール、ガバナンスおよびバッジ整備 | `COMP-004`, `COMP-005` | `docs/ci-and-repo-automation-spec` | 1 |
| **PR 2** | `feat/ci-phase2-repo-automations` | マルチエコシステム Dependabot およびセマンティックリリース | `COMP-002`, `COMP-003` | `PR 1` | 2 |
| **PR 3** | `feat/ci-phase3-ci-pipeline` | 完全な CI 品質ゲート（並列バックエンド/フロントエンド + E2E ゲート） | `COMP-001` | `PR 2` | 3 |

---

## 2. 追跡可能性マトリクス（Mechanical Traceability Matrix）

すべての要件（`REQ-001` 〜 `REQ-011`）および設計コンポーネント（`COMP-001` 〜 `COMP-005`）が漏れなく（ギャップゼロで）対応付けられています:

| 要件 ID | コンポーネント ID | 実装タスク | 対象 PR | ステータス |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-001` | `TASK-006` | PR 3 | 保留中（Pending） |
| **REQ-002** | `COMP-001` | `TASK-007` | PR 3 | 保留中（Pending） |
| **REQ-003** | `COMP-001` | `TASK-007` | PR 3 | 保留中（Pending） |
| **REQ-004** | `COMP-002` | `TASK-004` | PR 2 | 保留中（Pending） |
| **REQ-005** | `COMP-003` | `TASK-005` | PR 2 | 保留中（Pending） |
| **REQ-006** | `COMP-003` | `TASK-005` | PR 2 | 保留中（Pending） |
| **REQ-007** | `COMP-004` | `TASK-001` | PR 1 | 保留中（Pending） |
| **REQ-008** | `COMP-004` | `TASK-001` | PR 1 | 保留中（Pending） |
| **REQ-009** | `COMP-004` | `TASK-002` | PR 1 | 保留中（Pending） |
| **REQ-010** | `COMP-005` | `TASK-003` | PR 1 | 保留中（Pending） |
| **REQ-011** | `COMP-005` | `TASK-003` | PR 1 | 保留中（Pending） |

---

## 3. スタック PR タスク仕様および進捗トラッカー（Stacked PR Task Specifications & Progress Tracker）

実装エージェントは**アトミックコミットループ（Atomic Commit Loop）**を使用して順次タスクを実行します:
1. 未チェックの最初のタスク（`- [ ]`）を選択する。
2. コードと合格する単体/統合テストまたは設定検証を実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）としてマークする。
4. `committing-changes` を実行して、コードと更新された `tasks.md` / `tasks.ja.md` をアトミックにコミットする。
5. 予期しない中断が発生した場合、未チェックの最初のタスクから即座に再開する。

### PR 1: 開発者ツール、ガバナンスおよびバッジ整備
- **ブランチ**: `feat/ci-phase1-dev-orchestration-and-governance`
- **マージ先**: `docs/ci-and-repo-automation-spec`

#### タスク
- [x] **TASK-001**: ルート npm パッケージおよび開発スクリプトの構成
  - **コンポーネント & 要件**: `COMP-004`, `REQ-007`, `REQ-008`
  - **対象ファイル**: `package.json`, `package-lock.json`
  - **受け入れ基準**:
    - [x] `private: true` および名前 `"swe-workflow-playground"` を持つルート `package.json` が作成されていること。
    - [x] `concurrently` および `wait-on` が devDependencies としてインストールされていること。
    - [x] `npm run dev` スクリプトが MySQL 用の `docker compose up -d` をオーケストレートし、バックエンド（`./gradlew run`）とフロントエンド（`npm run start:ja`）を並行実行すること。
    - [x] 明示的なロケール開発用の `npm run dev:ja` および `npm run dev:en` スクリプトが提供されていること。
    - [x] データベースヘルパースクリプト（`db:up`, `db:down`）およびルートテスト集約スクリプト（`test`, `test:backend`, `test:frontend`, `test:e2e`）が構成されていること。
  - **コミットメッセージ**: `feat(app): configure root npm workspace and consolidated dev scripts`

- [x] **TASK-002**: プロセスライフサイクル、エラーハンドリング、および終了トラップの実装
  - **コンポーネント & 要件**: `COMP-004`, `REQ-009`
  - **対象ファイル**: `package.json`
  - **受け入れ基準**:
    - [x] エラー時または終了時にすべての兄弟プロセスを終了させるため、`concurrently` に `--kill-others`（`-k`）が構成されていること。
    - [x] 色分けされた識別可能なプロセスプレフィックス（`[backend]` および `[frontend]`）によるインターリーブ出力が構成されていること。
    - [x] `SIGINT` 送信時に孤立したプロセスを残さずに Java および Node の子プロセスがクリーンに停止することが検証テストで確認されていること。
  - **コミットメッセージ**: `feat(app): configure clean process lifecycle and signal trapping`

- [x] **TASK-003**: 正規 MIT ライセンスの作成、ドキュメントバッジ、および開発者ガイドの更新
  - **コンポーネント & 要件**: `COMP-005`, `REQ-010`, `REQ-011`
  - **対象ファイル**: `LICENSE`, `README.md`, `README.ja.md`, `AGENTS.md`
  - **受け入れ基準**:
    - [x] リポジトリルートに標準の 2026 年付 MIT ライセンス本文を含む正規の `LICENSE` ファイルが作成されていること。
    - [x] `README.md` および `README.ja.md` において、要求された正確な順序でバッジが構成されていること: (1) 最新リリース、(2) CI ステータス、(3) release-please ステータス、(4) Dependabot ステータス、(5) MIT ライセンス。
    - [x] `AGENTS.md` の Quick Start セクションが、単一情報源の技術ガイドラインと単方向参照の完全性を厳密に保持しながら、`npm run dev` ワークフローで更新されていること。
    - [x] 自動マークダウンリンクチェックにより、ドキュメントファイル間のリンク切れやアンカードリフトがゼロであることが確認されていること。
  - **コミットメッセージ**: `docs(app): add MIT license, status badges, and consolidated quickstart instructions`

---

### PR 2: マルチエコシステム Dependabot およびセマンティックリリース
- **ブランチ**: `feat/ci-phase2-repo-automations`
- **マージ先**: `feat/ci-phase1-dev-orchestration-and-governance`

#### タスク
- [x] **TASK-004**: マルチエコシステム Dependabot 自動化の構成
  - **コンポーネント & 要件**: `COMP-002`, `REQ-004`
  - **対象ファイル**: `.github/dependabot.yml`
  - **受け入れ基準**:
    - [x] スキーマバージョン 2 に厳格に準拠して `.github/dependabot.yml` が作成されていること。
    - [x] 正確に 4 つのパッケージエコシステム（`/backend` の `gradle`、`/frontend` の `npm`、`/` の `github-actions`、`/` の `docker`）が構成されていること。
    - [x] 4 つのエコシステムすべてが週次月曜日のスケジュール（`interval: "weekly"`, `day: "monday"`）に同期されていること。
    - [x] スキーマリンターによって Dependabot 設定構文が検証されていること。
  - **コミットメッセージ**: `ci(dependabot): configure multi-ecosystem weekly dependency automation`

- [x] **TASK-005**: セマンティック release-please ワークフローおよびマニフェストの構成
  - **コンポーネント & 要件**: `COMP-003`, `REQ-005`, `REQ-006`
  - **対象ファイル**: `.github/workflows/release-please.yml`, `.github/release-please-config.json`, `.release-please-manifest.json`
  - **受け入れ基準**:
    - [x] `main` へのプッシュで起動し、`contents: write` および `pull-requests: write` 権限を持つ `.github/workflows/release-please.yml` が構成されていること。
    - [x] ルートパッケージ `"."` に対して `release-type: "simple"` が設定された `.github/release-please-config.json` が構成されていること。
    - [x] 現在のリポジトリバージョンマイルストーン（`"0.1.0"`）で `.release-please-manifest.json` が初期化されていること。
    - [x] 静的リンティングによってワークフロー YAML および設定 JSON スキーマが検証されていること。
  - **コミットメッセージ**: `ci(release): configure google release-please semantic versioning and changelog automation`

---

### PR 3: 完全な CI 品質ゲート（並列バックエンド/フロントエンド + E2E ゲート）
- **ブランチ**: `feat/ci-phase3-ci-pipeline`
- **マージ先**: `feat/ci-phase2-repo-automations`

#### タスク
- [x] **TASK-006**: 並列バックエンドおよびフロントエンド CI 検証ジョブの実装
  - **コンポーネント & 要件**: `COMP-001`, `REQ-001`
  - **対象ファイル**: `.github/workflows/ci.yml`
  - **受け入れ基準**:
    - [x] `main` への `push` および `main` を対象とする `pull_request` で起動するように `.github/workflows/ci.yml` が構成されていること。
    - [x] 重複したランナー実行を防止するため、`cancel-in-progress: true` の concurrency グループが構成されていること。
    - [x] `backend` ジョブが `ubuntu-latest` 上で動作し、Gradle キャッシュ付きで Java 25 LTS（Corretto）をセットアップし、`./gradlew test`（Testcontainers MySQL 結合テスト含む）を実行すること。
    - [x] `frontend` ジョブが `ubuntu-latest` 上で動作し、npm キャッシュ付きで Node.js 22 LTS をセットアップし、`npm ci`、`npm test -- --watch=false`（Vitest）、および `npm run build` を実行すること。
    - [x] バックエンドジョブとフロントエンドジョブがジョブ間のブロックなしに並行して実行されること。
  - **コミットメッセージ**: `ci(workflow): implement parallel backend and frontend verification jobs`

- [x] **TASK-007**: 条件付き後続 E2E ゲートおよび成果物アップロードの実装
  - **コンポーネント & 要件**: `COMP-001`, `REQ-002`, `REQ-003`
  - **対象ファイル**: `.github/workflows/ci.yml`
  - **受け入れ基準**:
    - [x] 明示的な依存関係 `needs: [backend, frontend]` を持つ `e2e` ジョブが構成されていること。
    - [x] E2E ジョブが `docker compose up -d` で MySQL コンテナを起動し、バックグラウンドでバックエンドおよびフロントエンドサービスを起動し、タイムアウト付きで健全性チェックエンドポイントをポーリングすること。
    - [x] `npx playwright install --with-deps chromium` で Playwright Chromium ブラウザをインストールし、`npx playwright test` を実行すること。
    - [x] `backend` または `frontend` ジョブのいずれかが失敗した場合、`e2e` ジョブがスキップされ、ブラウザテストにランナーリソースが消費されないこと。
    - [x] テスト失敗時に、Playwright トレース、失敗スクリーンショット、およびサービスログが `actions/upload-artifact@v4` を介して GitHub Actions 成果物としてアップロードされること。
    - [x] actionlint または同等のワークフロー検証ツールによってワークフロー YAML が検証されていること。
  - **コミットメッセージ**: `ci(workflow): add conditional gated e2e verification and diagnostic artifact upload`

---

## 4. ライフサイクルおよびリセットプロトコル（Lifecycle & Reset Protocol）

- **初回作成時**: すべてのタスクは `- [ ]` として初期化されます。
- **仕様改訂時**:
  - 上記のすべてのタスクが完了（`- [x]`）している場合、この計画はアーカイブ/リセットされ、改訂差分のためのクリーンな新しいタスクリストに置き換えられます。
  - タスクが部分的に完了している場合、依存関係を再調整して進行中タスクをその場で更新します。
