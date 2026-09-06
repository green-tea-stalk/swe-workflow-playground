---
feature: bulletin-board
document_type: tasks
version: 1.0.0
status: draft
updated_at: 2026-09-05
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# 実装タスク計画書: 掲示板アプリケーション（Bulletin Board Application）

## 1. Stacked PR 概要（Executive Stacked PR Overview）

| PR # | フィーチャーブランチ | ターゲットブランチ（マージ先） | フェーズ / 目的 | 主要コンポーネント | 依存関係 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-phase1-backend-persistence` | `main` | インフラ構築 & 永続化層 | `COMP-003` | なし (`main`) | 1 |
| **PR 2** | `feat/bulletin-board-phase2-backend-api` | `feat/bulletin-board-phase1-backend-persistence` | ドメインロジック & REST API層 | `COMP-001`, `COMP-002` | PR 1 | 2 |
| **PR 3** | `feat/bulletin-board-phase3-frontend-ui` | `feat/bulletin-board-phase2-backend-api` | フロントエンドAngular UI & 統合 | `COMP-004`, `COMP-005`, `COMP-006`, `COMP-007` | PR 2 | 3 |

---

## 2. トレーサビリティ・マトリクス（Mechanical Traceability Matrix）

`requirements.md` のすべての要件と `design.md` のすべてのコンポーネントが、漏れなくマッピングされています：

| 要件ID | コンポーネントID | 実装タスク | 対象PR | 状態 |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-004`, `COMP-005` | `TASK-009`, `TASK-010` | PR 3 | 保留中（Pending） |
| **REQ-002** | `COMP-002`, `COMP-003`, `COMP-004`, `COMP-006` | `TASK-003`, `TASK-004`, `TASK-008`, `TASK-010` | PR 1, PR 2, PR 3 | 保留中（Pending） |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-006`, `COMP-007` | `TASK-003`, `TASK-004`, `TASK-006`, `TASK-007`, `TASK-008` | PR 1, PR 2, PR 3 | 保留中（Pending） |
| **REQ-004** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005`, `COMP-007` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006`, `TASK-007`, `TASK-009` | PR 1, PR 2, PR 3 | 保留中（Pending） |
| **REQ-005** | `COMP-004`, `COMP-005` | `TASK-009`, `TASK-010` | PR 3 | 保留中（Pending） |
| **REQ-006** | `COMP-001`, `COMP-005` | `TASK-005`, `TASK-006`, `TASK-009` | PR 2, PR 3 | 保留中（Pending） |
| **REQ-007** | `COMP-001`, `COMP-005` | `TASK-005`, `TASK-006`, `TASK-009` | PR 2, PR 3 | 保留中（Pending） |
| **REQ-008** | `COMP-006` | `TASK-008` | PR 3 | 保留中（Pending） |
| **REQ-009** | `COMP-001`, `COMP-002`, `COMP-007` | `TASK-004`, `TASK-005`, `TASK-007` | PR 2, PR 3 | 保留中（Pending） |

---

## 3. Stacked PR タスク仕様および進捗トラッカー

実装エージェントは、**アトミックコミットループ（Atomic Commit Loop）**を用いてタスクを順次実行します：
1. 先頭の未完了タスク（`- [ ]`）を選択する。
2. DbC契約に準拠したコードおよび合格する単体/統合テストを実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）に更新する。
4. `committing-changes` スキルを実行し、実装コードと更新された `tasks.md` をアトミックにコミットする。
5. 予期せぬ中断が発生した場合は、先頭の未完了タスクから直ちに再開する。

---

### PR 1: インフラ構築 & 永続化層
- **ブランチ**: `feat/bulletin-board-phase1-backend-persistence`
- **マージ先**: `main`

#### タスク
- [x] **TASK-001**: Docker Compose MySQL 環境および Gradle Micronaut ワークスペースの初期構築
  - **対象コンポーネント & 要件**: `COMP-003`, `NFR-DEV-001`, `NFR-COMP-001`
  - **対象ファイル**: `docker-compose.yml`, `backend/build.gradle`, `backend/settings.gradle`, `backend/gradlew`, `backend/src/main/resources/application.yml`
  - **受け入れ基準**:
    - [x] `docker-compose.yml` がMySQL 8.4 LTSをポート3306で構成し、ヘルスチェックおよび永続化ボリュームを定義していること。
    - [x] バックエンドGradleプロジェクトが、Micronaut 4.x、Micronaut Data JDBC、MySQL Connector/J、Flywayの依存関係を含んで正常にビルドできること。
    - [x] 基本設定がクラスパスエラーなくロードできること。
  - **コミットメッセージ**: `chore(infra): setup docker mysql and backend gradle workspace`

- [x] **TASK-002**: Flyway スキーママイグレーションおよび MessageEntity マッピングの実装
  - **対象コンポーネント & 要件**: `COMP-003`, `REQ-004`, `NFR-REL-001`
  - **対象ファイル**: `backend/src/main/resources/db/migration/V1__create_messages_table.sql`, `backend/src/main/java/com/example/bulletinboard/entity/MessageEntity.java`, `backend/src/test/java/com/example/bulletinboard/entity/MessageEntityTest.java`
  - **受け入れ基準**:
    - [x] SQL DDLが `messages` テーブルを作成し、主キー `id` (BIGINT AUTO_INCREMENT)、`name` (VARCHAR 50 NOT NULL)、`email` (VARCHAR 100 NULL)、`title` (VARCHAR 100 NOT NULL)、`message` (VARCHAR 1000 NOT NULL)、`created_at` (TIMESTAMP NOT NULL) および `created_at` 降順インデックスを定義していること。
    - [x] `MessageEntity` が Micronaut Data の `@MappedEntity("messages")`、`@Id`、`@GeneratedValue` を用いて正しくマッピングされていること。
    - [x] エンティティの単体テストが不変性、フィールドゲッター、equals/hashCode契約を検証していること。
  - **コミットメッセージ**: `feat(backend): add flyway migration and message entity`

- [x] **TASK-003**: ページネーションクエリおよび統合テストを含む MessageRepository の実装
  - **対象コンポーネント & 要件**: `COMP-003`, `REQ-002`, `REQ-003`, `REQ-004`
  - **対象ファイル**: `backend/src/main/java/com/example/bulletinboard/repository/MessageRepository.java`, `backend/src/test/java/com/example/bulletinboard/repository/MessageRepositoryTest.java`
  - **受け入れ基準**:
    - [x] `MessageRepository` が `@JdbcRepository(dialect = Dialect.MYSQL)` を伴って `PageableRepository<MessageEntity, Long>` を継承していること。
    - [x] 事前条件・事後条件が検証されていること: `save` がIDおよび作成日時を生成し、`findAll(Pageable)` が `created_at DESC` でソートされたレコードを返却すること。
    - [x] リポジトリ統合テストが、MySQL（またはテスト用H2/Testcontainers）に対してページネーションと逆時系列ソートを検証していること。
  - **コミットメッセージ**: `feat(backend): implement COMP-003 message repository`

---

### PR 2: ドメインロジック & REST API層
- **ブランチ**: `feat/bulletin-board-phase2-backend-api`
- **マージ先**: `feat/bulletin-board-phase1-backend-persistence`

#### タスク
- [x] **TASK-004**: ドメイン調整およびトランザクション境界を担う MessageService の実装
  - **対象コンポーネント & 要件**: `COMP-002`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-009`, `NFR-REL-001`
  - **対象ファイル**: `backend/src/main/java/com/example/bulletinboard/service/MessageService.java`, `backend/src/main/java/com/example/bulletinboard/dto/MessageCreateRequest.java`, `backend/src/main/java/com/example/bulletinboard/dto/MessageResponse.java`, `backend/src/main/java/com/example/bulletinboard/dto/PageResponse.java`, `backend/src/test/java/com/example/bulletinboard/service/MessageServiceTest.java`
  - **受け入れ基準**:
    - [x] DTOレコードが定義されていること: `MessageCreateRequest`, `MessageResponse`, `PageResponse<T>`。
    - [x] `createMessage` がトランザクション境界内で実行され、入力をトリムし、エンティティへマッピングして事後条件のレスポンスを保証していること。
    - [x] `findMessages` が作成日時の降順でソートされた `PageResponse<MessageResponse>` を返却すること。
    - [x] 単体テストが正常系およびデータベース例外伝搬のすべての分岐を網羅していること。
  - **コミットメッセージ**: `feat(backend): implement COMP-002 message service and DTOs`

- [x] **TASK-005**: RFC 9457 Problem Details ハンドラーおよび例外階層の実装
  - **対象コンポーネント & 要件**: `COMP-001`, `REQ-006`, `REQ-007`, `REQ-009`
  - **対象ファイル**: `backend/src/main/java/com/example/bulletinboard/exception/BulletinBoardException.java`, `backend/src/main/java/com/example/bulletinboard/exception/ProblemDetails.java`, `backend/src/main/java/com/example/bulletinboard/exception/ValidationExceptionHandler.java`, `backend/src/main/java/com/example/bulletinboard/exception/GlobalExceptionHandler.java`, `backend/src/test/java/com/example/bulletinboard/exception/ExceptionHandlerTest.java`
  - **受け入れ基準**:
    - [x] `ProblemDetails` モデルが RFC 9457 (`type`, `title`, `status`, `detail`, `instance`, `invalid_params`) に準拠していること。
    - [x] `ValidationExceptionHandler` が Micronaut の `ConstraintViolationException` を構造化フィールドエラーを含む HTTP 400 Problem Details に変換すること。
    - [x] `GlobalExceptionHandler` が根本原因をログ出力し、スタックトレースを漏洩させずに HTTP 500 Problem Details を返却すること。
    - [x] 単体テストがバリデーション失敗および予期せぬ例外の JSON エラーペイロードを検証していること。
  - **コミットメッセージ**: `feat(backend): add RFC 9457 problem details exception handlers`

- [x] **TASK-006**: MessageController REST エンドポイントおよび CORS 設定の実装
  - **対象コンポーネント & 要件**: `COMP-001`, `REQ-003`, `REQ-004`, `REQ-006`, `REQ-007`, `NFR-PERF-001`, `NFR-SEC-001`
  - **対象ファイル**: `backend/src/main/java/com/example/bulletinboard/controller/MessageController.java`, `backend/src/test/java/com/example/bulletinboard/controller/MessageControllerTest.java`
  - **受け入れ基準**:
    - [x] `GET /api/messages` が `page`（デフォルト: 0）および `size`（デフォルト: 50）を処理し、`PageResponse` と共に `200 OK` を返却すること。
    - [x] `POST /api/messages` が `@Valid` によりリクエスト本文を検証し、`MessageResponse` と共に `201 Created` を返却すること。
    - [x] `http://localhost:4200` に対する CORS が許可されたメソッド（`GET`, `POST`, `OPTIONS`）で有効化されていること。
    - [x] コントローラー統合テストが HTTP ステータスコード、リクエスト検証の拒絶、およびレスポンスの正常なシリアライズを検証していること。
  - **コミットメッセージ**: `feat(backend): implement COMP-001 message controller and endpoints`

---

### PR 3: フロントエンド Angular UI & 統合
- **ブランチ**: `feat/bulletin-board-phase3-frontend-ui`
- **マージ先**: `feat/bulletin-board-phase2-backend-api`

#### タスク
- [x] **TASK-007**: Angular ワークスペースの初期化、Angular Material テーマおよび MessageApiClient の実装
  - **対象コンポーネント & 要件**: `COMP-007`, `REQ-003`, `REQ-004`, `REQ-009`, `NFR-COMP-001`
  - **対象ファイル**: `frontend/package.json`, `frontend/angular.json`, `frontend/src/app/models/message.model.ts`, `frontend/src/app/services/message-api.service.ts`, `frontend/src/app/services/message-api.service.spec.ts`
  - **受け入れ基準**:
    - [x] Standalone Components および Angular Material コンポーネント（`MatCard`, `MatButton`, `MatFormField`, `MatInput`, `MatPaginator`）を備えた Angular プロジェクトが初期化されていること。
    - [x] TypeScript インターフェースが定義されていること: `MessageCreateRequest`, `MessageResponse`, `PageResponse<T>`, `ProblemDetails`。
    - [x] `MessageApiClient`（COMP-007）が `HttpClient` を用いて `getMessages` および `postMessage` を実装していること。
    - [x] `HttpTestingController` を用いた単体テストがクエリパラメータ、ペイロード、エラー変換を検証していること。
  - **コミットメッセージ**: `feat(frontend): initialize angular workspace and COMP-007 api client`

- [x] **TASK-008**: メッセージ一覧描画、ページネーション、メール表示を含む MessageListComponent の実装
  - **対象コンポーネント & 要件**: `COMP-006`, `REQ-002`, `REQ-003`, `REQ-008`, `NFR-SEC-001`
  - **対象ファイル**: `frontend/src/app/components/message-list/message-list.component.ts`, `frontend/src/app/components/message-list/message-list.component.html`, `frontend/src/app/components/message-list/message-list.component.scss`, `frontend/src/app/components/message-list/message-list.component.spec.ts`
  - **受け入れ基準**:
    - [x] コンポーネントが `@Input() messages` シグナルから受信した順序通りにメッセージを描画すること。
    - [x] 投稿者メールアドレスが存在する場合は `mailto:` リンクとして描画し、null/空の場合は空文字の遺物を残さず完全に省略すること。
    - [x] `MatPaginator` が `totalElements`、`pageSize = 50` を表示し、0開始のページ変更イベントを上位へ通知すること。
    - [x] コンポーネント単体テストがフィード描画、メールアドレスの有無、およびページネーションイベント発行を検証していること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-006 message list and paginator`

- [x] **TASK-009**: 画面下部固定レイアウトおよびリアクティブバリデーションを含む MessageFormComponent の実装
  - **対象コンポーネント & 要件**: `COMP-005`, `REQ-001`, `REQ-004`, `REQ-006`, `REQ-007`
  - **対象ファイル**: `frontend/src/app/components/message-form/message-form.component.ts`, `frontend/src/app/components/message-form/message-form.component.html`, `frontend/src/app/components/message-form/message-form.component.scss`, `frontend/src/app/components/message-form/message-form.component.spec.ts`
  - **受け入れ基準**:
    - [x] リアクティブフォームが制約を強制すること: 名前（必須、1〜50文字）、メールアドレス（任意、メール形式、最大100文字）、タイトル（必須、1〜100文字）、メッセージ（必須、1〜1,000文字）。
    - [x] フォームが入力不正時の送信を防止し、項目ごとのエラーヒントを表示すること。
    - [x] コンポーネントCSSが画面最下部への固定配置スタイル（`position: sticky`, `bottom: 0`, `z-index: 100`）を適用していること。
    - [x] コンポーネント単体テストがバリデーション規則、送信イベント発行、および `resetForm()` の挙動を検証していること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-005 sticky message form`

- [ ] **TASK-010**: 状態連携、自動再取得、独立スクロールを統括する BulletinBoardComponent ルートコンテナの実装
  - **対象コンポーネント & 要件**: `COMP-004`, `REQ-001`, `REQ-002`, `REQ-005`
  - **対象ファイル**: `frontend/src/app/components/bulletin-board/bulletin-board.component.ts`, `frontend/src/app/components/bulletin-board/bulletin-board.component.html`, `frontend/src/app/components/bulletin-board/bulletin-board.component.scss`, `frontend/src/app/components/bulletin-board/bulletin-board.component.spec.ts`, `frontend/src/app/app.component.ts`
  - **受け入れ基準**:
    - [ ] ルートコンテナが上部スクロール可能領域（`overflow-y: auto`）の `MessageListComponent` と最下部に固定された `MessageFormComponent` を統括配置すること。
    - [ ] 初期化時に先頭ページ（`page=0`）を自動取得すること。
    - [ ] メッセージ投稿成功時にフォームをリセットし、新規投稿を最上部に表示するため先頭ページを再取得すること。
    - [ ] コンポーネント統合テストが、コンポーネント間の連携、再取得トリガー、およびエラーバナー表示を検証していること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-004 bulletin board root container`

---

## 4. ライフサイクルおよびリセットプロトコル（Lifecycle & Reset Protocol）

- **初回作成時**: すべてのタスクは未完了（`- [ ]`）として初期化される。
- **仕様改訂時**:
  - 上記のすべてのタスクが完了（`- [x]`）している場合、本タスクリストはアーカイブ/リセットされ、改訂差分に応じた新しいタスク一覧で再構築される。
  - タスクが部分的に完了している場合、依存関係を再調整した上でアクティブなタスクがインプレースで更新される。
