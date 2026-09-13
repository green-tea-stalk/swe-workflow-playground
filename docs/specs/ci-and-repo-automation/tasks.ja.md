---
feature: ci-and-repo-automation
document_type: tasks
version: 1.1.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.1.0
  design: 1.1.0
---

# 実装タスク計画書: CI およびリポジトリ自動化（CI & Repository Automation）

## 1. エグゼクティブ Stacked PR 概要（Executive Stacked PR Overview）

| PR # | ターゲットブランチ | フェーズ / 目的 | 主要コンポーネント | 依存関係 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/ci-phase1-dev-orchestration-and-governance` | 開発者ツール、ガバナンス & バッジ | `COMP-004`, `COMP-005` | `docs/ci-and-repo-automation-spec` | 完了 (v1.0.0) |
| **PR 2** | `feat/ci-phase2-repo-automations` | 複数エコシステム Dependabot & セマンティックリリース | `COMP-002`, `COMP-003` | `PR 1` | 完了 (v1.0.0) |
| **PR 3** | `feat/ci-phase3-ci-pipeline` | フル CI 品質ゲート (並行バックエンド/フロントエンド + ゲート付き E2E) | `COMP-001` | `PR 2` | 完了 (v1.0.0) |
| **PR 4** | `feat/ci-phase4-governance-and-version-catalog` | コードオーナーシップガバナンス & Gradle Version Catalog | `COMP-006`, `COMP-008` | `main` | 1 (進行中) |
| **PR 5** | `feat/ci-phase5-formatters-and-cleanup` | バックエンド Spotless & フロントエンド Prettier による一括コード整形 | `COMP-006`, `COMP-007` | `PR 4` | 2 (進行中) |
| **PR 6** | `feat/ci-phase6-ci-format-gates` | CI フォーマット品質ゲート & 開発者ガイド更新 | `COMP-001`, `COMP-005` | `PR 5` | 3 (進行中) |

---

## 2. 機械的トレーサビリティマトリクス（Mechanical Traceability Matrix）

すべての要件（`REQ-001` 〜 `REQ-016`）および設計コンポーネント（`COMP-001` 〜 `COMP-008`）に漏れなく対応しています：

| 要件 ID | コンポーネント ID | 実装タスク | 対象 PR | ステータス |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-001` | `TASK-006`, `TASK-012` | PR 3, PR 6 | 完了 (PR 3) / 保留中 (PR 6) |
| **REQ-002** | `COMP-001` | `TASK-007` | PR 3 | 完了 |
| **REQ-003** | `COMP-001` | `TASK-007` | PR 3 | 完了 |
| **REQ-004** | `COMP-002` | `TASK-004` | PR 2 | 完了 |
| **REQ-005** | `COMP-003` | `TASK-005` | PR 2 | 完了 |
| **REQ-006** | `COMP-003` | `TASK-005` | PR 2 | 完了 |
| **REQ-007** | `COMP-004` | `TASK-001` | PR 1 | 完了 |
| **REQ-008** | `COMP-004` | `TASK-001` | PR 1 | 完了 |
| **REQ-009** | `COMP-004` | `TASK-002` | PR 1 | 完了 |
| **REQ-010** | `COMP-005` | `TASK-003`, `TASK-013` | PR 1, PR 6 | 完了 (PR 1) / 保留中 (PR 6) |
| **REQ-011** | `COMP-005` | `TASK-003` | PR 1 | 完了 |
| **REQ-012** | `COMP-006` | `TASK-010` | PR 5 | 保留中 |
| **REQ-013** | `COMP-007` | `TASK-011` | PR 5 | 保留中 |
| **REQ-014** | `COMP-008` | `TASK-008` | PR 4 | 完了 |
| **REQ-015** | `COMP-006` | `TASK-009` | PR 4 | 完了 |
| **REQ-016** | `COMP-001`, `COMP-006`, `COMP-007` | `TASK-010`, `TASK-011`, `TASK-012` | PR 5, PR 6 | 保留中 |

---

## 3. Stacked PR タスク仕様と進捗トラッカー（Stacked PR Task Specifications & Progress Tracker）

実装エージェントは**アトミックコミットループ（Atomic Commit Loop）**を使用してタスクを順次実行します：
1. 最初の未完了タスク（`- [ ]`）を選択する。
2. コードおよび合格する単体/結合テスト、または構成検証を実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）としてマークする。
4. `committing-changes` を実行して、コードと更新された `tasks.md` をアトミックにコミットする。
5. 予期しない中断が発生した場合は、最初の未完了タスクから直ちに再開する。

---

### アーカイブされたマイルストーンタスク (バージョン 1.0.0 - 完了済み)

<details>
<summary>完了した v1.0.0 タスク (TASK-001 〜 TASK-007) を表示</summary>

#### PR 1: 開発者ツール、ガバナンス & バッジ (完了)
- [x] **TASK-001**: ルート npm パッケージおよび開発スクリプトの構成
  - **コンポーネント & 要件**: `COMP-004`, `REQ-007`, `REQ-008`
  - **対象ファイル**: `package.json`, `package-lock.json`
  - **受け入れ基準**:
    - [x] `private: true` および名前 `"swe-workflow-playground"` を持つルート `package.json` が作成されていること。
    - [x] `concurrently` および `wait-on` が devDependencies としてインストールされていること。
    - [x] `npm run dev` スクリプトが MySQL 用の `docker compose up -d` をオーケストレーションし、その後バックエンド（`./gradlew run`）とフロントエンド（`npm run start:ja`）を並行実行すること。
    - [x] 明示的なターゲットロケール開発用に `npm run dev:ja` および `npm run dev:en` スクリプトが提供されていること。
    - [x] データベースヘルパースクリプト（`db:up`, `db:down`）およびルートテスト集約スクリプト（`test`, `test:backend`, `test:frontend`, `test:e2e`）が構成されていること。
  - **コミットメッセージ**: `feat(app): configure root npm workspace and consolidated dev scripts`

- [x] **TASK-002**: プロセスライフサイクル、エラーハンドリング、および終了トラップの実装
  - **コンポーネント & 要件**: `COMP-004`, `REQ-009`
  - **対象ファイル**: `package.json`
  - **受け入れ基準**:
    - [x] エラーまたは終了時にすべての兄弟プロセスを確実に終了させるために、`concurrently` が `--kill-others`（`-k`）付きで構成されていること。
    - [x] 色分けされた識別可能なプロセスプレフィックス（`[backend]` および `[frontend]`）によるインターリーブ出力フォーマットが構成されていること。
    - [x] 検証テストにより、`SIGINT` 送信時に孤立したプロセスを残さずに Java および Node の子プロセスが正常に停止することが確認されていること。
  - **コミットメッセージ**: `feat(app): configure clean process lifecycle and signal trapping`

- [x] **TASK-003**: 正規 MIT ライセンス、ドキュメントバッジ、および開発者ガイド更新の確立
  - **コンポーネント & 要件**: `COMP-005`, `REQ-010`, `REQ-011`
  - **対象ファイル**: `LICENSE`, `README.md`, `README.ja.md`, `AGENTS.md`
  - **受け入れ基準**:
    - [x] リポジトリルートに標準 MIT ライセンス本文（2026 年）を含む正規の `LICENSE` ファイルが作成されていること。
    - [x] `README.md` および `README.ja.md` において、（1）最新リリース、（2）CI ステータス、（3）release-please ステータス、（4）Dependabot ステータス、（5）MIT ライセンス、という厳密に要求された順序でバッジが構成されていること。
    - [x] 単一情報源ポリシーおよび一方向参照の整合性を厳格に維持しながら、`AGENTS.md` のクイックスタートセクションが主要な起動手順として `npm run dev` ワークフローで更新されていること。
    - [x] 自動マークダウンリンクチェックにより、ドキュメントファイル全体でリンク切れやアンカーの乖離がゼロであることが確認されていること。
  - **コミットメッセージ**: `docs(app): add MIT license, status badges, and consolidated quickstart instructions`

#### PR 2: 複数エコシステム Dependabot & セマンティックリリース (完了)
- [x] **TASK-004**: 複数エコシステム Dependabot 自動化の構成
  - **コンポーネント & 要件**: `COMP-002`, `REQ-004`
  - **対象ファイル**: `.github/dependabot.yml`
  - **受け入れ基準**:
    - [x] スキーマバージョン 2 に厳格に準拠した `.github/dependabot.yml` が作成されていること。
    - [x] 正確に 4 つのパッケージエコシステム（`gradle` (`/backend`)、`npm` (`/frontend`)、`github-actions` (`/`)、`docker` (`/`)）が構成されていること。
    - [x] 4 つのすべてのエコシステムが毎週月曜日のスケジュール（`interval: "weekly"`, `day: "monday"`）に同期されていること。
    - [x] スキーマリンターによって Dependabot 構成構文が検証されていること。
  - **コミットメッセージ**: `ci(dependabot): configure multi-ecosystem weekly dependency automation`

- [x] **TASK-005**: セマンティック release-please ワークフローおよびマニフェストの構成
  - **コンポーネント & 要件**: `COMP-003`, `REQ-005`, `REQ-006`
  - **対象ファイル**: `.github/workflows/release-please.yml`, `.github/release-please-config.json`, `.release-please-manifest.json`
  - **受け入れ基準**:
    - [x] `contents: write` および `pull-requests: write` 権限を持ち、`main` へのプッシュでトリガーされるように `.github/workflows/release-please.yml` が構成されていること。
    - [x] ルートパッケージ `"."` に対して `release-type: "simple"` を設定した `.github/release-please-config.json` が構成されていること。
    - [x] 現在のリポジトリバージョンマイルストーン（`"0.1.0"`）で `.release-please-manifest.json` が初期化されていること。
    - [x] 静的リントにより、ワークフロー YAML および構成 JSON スキーマが検証されていること。
  - **コミットメッセージ**: `ci(release): configure google release-please semantic versioning and changelog automation`

#### PR 3: フル CI 品質ゲート (完了)
- [x] **TASK-006**: 並行バックエンドおよびフロントエンド CI 検証ジョブの実装
  - **コンポーネント & 要件**: `COMP-001`, `REQ-001`
  - **対象ファイル**: `.github/workflows/ci.yml`
  - **受け入れ基準**:
    - [x] `main` への `push` および `main` を対象とする `pull_request` でトリガーされるように `.github/workflows/ci.yml` が構成されていること。
    - [x] 冗長なランナー実行を防止するために、`cancel-in-progress: true` を持つ並行実行グループが構成されていること。
    - [x] `backend` ジョブが `ubuntu-latest` 上で実行され、Gradle キャッシュ付きで Java 25 LTS（Corretto）をセットアップし、`./gradlew test` を実行すること。
    - [x] `frontend` ジョブが `ubuntu-latest` 上で実行され、npm キャッシュ付きで Node.js 22 LTS をセットアップし、`npm ci` を実行し、`npm test -- --watch=false` を実行し、`npm run build` を実行すること。
    - [x] バックエンドジョブとフロントエンドジョブが相互ブロッキングなしに並行して実行されること。
  - **コミットメッセージ**: `ci(workflow): implement parallel backend and frontend verification jobs`

- [x] **TASK-007**: 条件付き下流 E2E ゲートおよびアーティファクトアップロードの実装
  - **コンポーネント & 要件**: `COMP-001`, `REQ-002`, `REQ-003`
  - **対象ファイル**: `.github/workflows/ci.yml`
  - **受け入れ基準**:
    - [x] 明示的な依存関係 `needs: [backend, frontend]` を持つ `e2e` ジョブが構成されていること。
    - [x] E2E ジョブが `docker compose up -d` を介して MySQL コンテナを起動し、バックグラウンドでバックエンドおよびフロントエンドサービスを起動し、タイムアウト付きでヘルスチェックエンドポイントをポーリングすること。
    - [x] `npx playwright install --with-deps chromium` を介して Playwright Chromium ブラウザがインストールされ、`npx playwright test` が実行されること。
    - [x] `backend` または `frontend` ジョブのいずれかが失敗した場合、`e2e` ジョブがスキップされ、ブラウザテストにランナーリソースが消費されないこと。
    - [x] テスト失敗時に、Playwright トレース、失敗スクリーンショット、およびサービスログが `actions/upload-artifact@v4` 経由で GitHub Actions アーティファクトとしてアップロードされること。
    - [x] ワークフロー YAML が構文チェックをパスすること。
  - **コミットメッセージ**: `ci(workflow): add conditional gated e2e verification and diagnostic artifact upload`

</details>

---

### アクティブなマイルストーンタスク (バージョン 1.1.0)

### PR 4: コードオーナーシップガバナンス & Gradle Version Catalog
- **ブランチ**: `feat/ci-phase4-governance-and-version-catalog`
- **マージ先**: `main`

#### タスク
- [x] **TASK-008**: リポジトリ全体の CODEOWNERS 構成の確立
  - **コンポーネント & 要件**: `COMP-008`, `REQ-014`
  - **対象ファイル**: `.github/CODEOWNERS`
  - **受け入れ基準**:
    - [x] リポジトリルートに `.github/CODEOWNERS` ファイルが作成されていること。
    - [x] すべてのファイルにデフォルトのレビュー責任を割り当てるワイルドカードルール `* @green-tea-stalk` が構成されていること。
    - [x] 検証チェックにより、ファイル構文および GitHub ユーザー名の一致が確認されていること。
  - **コミットメッセージ**: `chore(governance): add repository-wide CODEOWNERS assigning @green-tea-stalk`

- [x] **TASK-009**: バックエンド依存関係およびプラグイン用 Gradle Version Catalog の実装
  - **コンポーネント & 要件**: `COMP-006`, `REQ-015`
  - **対象ファイル**: `backend/gradle/libs.versions.toml`, `backend/build.gradle.kts`
  - **受け入れ基準**:
    - [x] `[versions]`, `[libraries]`, `[plugins]` セクションを含む `backend/gradle/libs.versions.toml` が作成されていること。
    - [x] すべてのバックエンドプラグイン（`io.micronaut.application`, `com.gradleup.shadow`, `io.micronaut.aot`）および依存関係がカタログ化されていること。
    - [x] ハードコードされた依存関係バージョン文字列を排除し、型安全な `libs.plugins...` および `libs...` アクセサを使用するように `backend/build.gradle.kts` がリファクタリングされていること。
    - [x] プラットフォーム環境バージョン（Java 25 LTS, MySQL 8.4 LTS）がカタログから厳格に除外されていること。
    - [x] `./gradlew buildEnvironment` および `./gradlew test` の実行が解決エラーなしで成功すること。
  - **コミットメッセージ**: `build(backend): introduce gradle version catalog and migrate build dependencies`

---

### PR 5: バックエンド Spotless & フロントエンド Prettier による一括コード整形
- **ブランチ**: `feat/ci-phase5-formatters-and-cleanup`
- **マージ先**: `feat/ci-phase4-governance-and-version-catalog`

#### タスク
- [x] **TASK-010**: Java（Palantir）および Kotlin DSL（ktlint）用 Spotless コードフォーマットの構成
  - **コンポーネント & 要件**: `COMP-006`, `REQ-012`, `REQ-016`
  - **対象ファイル**: `backend/gradle/libs.versions.toml`, `backend/build.gradle.kts`, `backend/src/**/*.java`
  - **受け入れ基準**:
    - [x] Spotless Gradle プラグイン座標が `backend/gradle/libs.versions.toml` に追加され、`backend/build.gradle.kts` に適用されていること。
    - [x] Java ソース（`src/**/*.java`、4 スペース）用に `palantirJavaFormat()`、Kotlin Gradle スクリプト（`*.gradle.kts`）用に `ktlint()` で Spotless が構成されていること。
    - [x] 既存のすべての Java ファイルおよび Gradle スクリプトをその場で整形するために、初回の `./gradlew spotlessApply` が実行されていること。
    - [x] `./gradlew spotlessCheck` の実行が終了コード `0` で成功すること。
    - [x] 意図的にスタイル違反を混入させると、`./gradlew spotlessCheck` が終了コード `1` で失敗すること。
  - **コミットメッセージ**: `style(backend): configure spotless with palantir java format and format codebase`

- [x] **TASK-011**: フロントエンド用 Prettier フォーマットおよびライフサイクルスクリプトの構成
  - **コンポーネント & 要件**: `COMP-007`, `REQ-013`, `REQ-016`
  - **対象ファイル**: `frontend/.prettierrc`, `frontend/.prettierignore`, `frontend/package.json`, `frontend/src/**/*`
  - **受け入れ基準**:
    - [x] `tabWidth: 2`, `singleQuote: true`, `semi: true`, `trailingComma: "all"`, `printWidth: 100` を持つ `frontend/.prettierrc` が作成されていること。
    - [x] `dist/`, `.angular/`, `node_modules/`, `coverage/` を除外する `frontend/.prettierignore` が作成されていること。
    - [x] `frontend/package.json` スクリプトに `format:check`（`prettier --check .`）および `format`（`prettier --write .`）が追加されていること。
    - [x] `frontend/` 内のすべての TypeScript、HTML、SCSS、および JSON ファイルにわたり、初回の `npm run format` が実行されていること。
    - [x] `npm run format:check` の実行が終了コード `0` で成功すること。
    - [x] 意図的にフォーマットの不一致を混入させると、`npm run format:check` が終了コード `1` で失敗すること。
  - **コミットメッセージ**: `style(frontend): configure prettier rules and apply in-place formatting`

---

### PR 6: CI フォーマット品質ゲート & 開発者ガイド更新
- **ブランチ**: `feat/ci-phase6-ci-format-gates`
- **マージ先**: `feat/ci-phase5-formatters-and-cleanup`

#### タスク
- [x] **TASK-012**: CI ワークフローへのフェイルファストフォーマット品質ゲートの統合
  - **コンポーネント & 要件**: `COMP-001`, `REQ-001`, `REQ-016`
  - **対象ファイル**: `.github/workflows/ci.yml`
  - **受け入れ基準**:
    - [x] `backend` ジョブにおいて、`./gradlew test` の実行前に `./gradlew spotlessCheck` ステップが追加されていること。
    - [x] `frontend` ジョブにおいて、`npm ci` の後、単体テストおよびビルドの前に `npm run format:check` ステップが追加されていること。
    - [x] いずれかのジョブでフォーマット違反が発生した場合、ジョブが非ゼロの終了コードで直ちに停止し、`e2e` ジョブがスキップされること。
    - [x] ワークフロー YAML が構文チェックをパスすること。
  - **コミットメッセージ**: `ci(workflow): add backend spotless and frontend prettier verification steps`

- [ ] **TASK-013**: 技術開発者ドキュメントおよび AGENTS.md 単一情報源の更新
  - **コンポーネント & 要件**: `COMP-005`, `REQ-010`
  - **対象ファイル**: `AGENTS.md`, `README.md`, `README.ja.md`
  - **受け入れ基準**:
    - [ ] フォーマット検証および修正コマンド（`./gradlew spotlessCheck`, `./gradlew spotlessApply`, `npm run format:check`, `npm run format`）で `AGENTS.md` 第 5 セクションが更新されていること。
    - [ ] 単一情報源ポリシーおよび一方向参照の整合性を維持しながら、Version Catalog アーキテクチャおよび CODEOWNERS レビュー割り当てを文書化するように `AGENTS.md` が更新されていること。
    - [ ] すべての相互参照およびアンカーが検証され、リンク切れがゼロであること。
  - **コミットメッセージ**: `docs(app): document formatting commands and version catalog architecture in AGENTS.md`

---

## 4. ライフサイクルとリセットプロトコル（Lifecycle & Reset Protocol）

- **初期作成時**: すべてのタスクは `- [ ]` として初期化されます。
- **仕様改訂時**:
  - 以前に完了したタスク（`TASK-001` 〜 `TASK-007`）は完了マイルストーンセクションにアーカイブされます。
  - 改訂 `1.1.0` のアクティブなタスク（`TASK-008` 〜 `TASK-013`）は GFM チェックボックスで追跡され、アトミックコミットループを介して実行されます。
