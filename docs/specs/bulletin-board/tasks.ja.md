---
feature: bulletin-board
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-11
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# 実装タスク計画書: 掲示板アプリケーション（Bulletin Board Application）

## 1. Stacked PR 概要（Executive Stacked PR Overview）

| PR 番号 | 対象ブランチ | フェーズ / 目的 | 主要コンポーネント | 依存関係 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-phase1-backend-persistence` | バックエンド基盤構築、Flyway マイグレーション & 永続化 | `COMP-003` | `docs/bulletin-board-spec` | 1 |
| **PR 2** | `feat/bulletin-board-phase2-backend-service-api` | ドメインビジネスロジック & REST コントローラー (RFC 9457) | `COMP-001`, `COMP-002` | `PR 1` | 2 |
| **PR 3** | `feat/bulletin-board-phase3-frontend-feed` | Angular 基盤構築、フィード一覧表示 & 50件ページネーション | `COMP-004` | `PR 2` | 3 |
| **PR 4** | `feat/bulletin-board-phase4-frontend-form-e2e` | 画面下部固定フォーム、フィード連携 & E2E 統合検証 | `COMP-005` | `PR 3` | 4 |

---

## 2. 要件・コンポーネント・タスク追跡マトリクス（Mechanical Traceability Matrix）

すべてのアクティブな要件および設計コンポーネントは、抜け漏れなく網羅されています。

| 要件 ID | コンポーネント ID | 実装タスク | 対象 PR | ステータス |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-005` | `TASK-007` | PR 4 | 保留中（Pending） |
| **REQ-002** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006` | PR 1, PR 2, PR 3 | 保留中（Pending） |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006` | PR 1, PR 2, PR 3 | 保留中（Pending） |
| **REQ-004** | `COMP-004` | `TASK-006` | PR 3 | 保留中（Pending） |
| **REQ-005** | `COMP-004` | `TASK-006` | PR 3 | 保留中（Pending） |
| **REQ-006** | `COMP-004` | `TASK-006` | PR 3 | 保留中（Pending） |
| **REQ-007** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-007` | PR 1, PR 2, PR 4 | 保留中（Pending） |
| **REQ-008** | `COMP-001`, `COMP-005` | `TASK-004`, `TASK-007` | PR 2, PR 4 | 保留中（Pending） |
| **REQ-009** | `COMP-001`, `COMP-005` | `TASK-004`, `TASK-007` | PR 2, PR 4 | 保留中（Pending） |
| **REQ-010** | `COMP-001`, `COMP-004`, `COMP-005` | `TASK-004`, `TASK-006`, `TASK-007`, `TASK-008` | PR 2, PR 3, PR 4 | 保留中（Pending） |

---

## 3. Stacked PR タスク仕様および進捗トラッカー（Progress Tracker）

実装エージェントは、**アトミックコミットループ（Atomic Commit Loop）** を使用してタスクを順次実行します。
1. 最初の未完了タスク（`- [ ]`）を選択する。
2. コードおよび合格する単体テスト・統合テストを実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）にする。
4. `committing-changes` を実行し、コードと更新された `tasks.md` をアトミックにコミットする。
5. 予期しない中断が発生した場合は、最初の未完了タスクから即座に再開する。

### PR 1: バックエンド基盤構築、Flyway マイグレーション & 永続化
- **ブランチ**: `feat/bulletin-board-phase1-backend-persistence`
- **マージ対象**: `docs/bulletin-board-spec`

#### タスク一覧
- [x] **TASK-001**: Micronaut バックエンドおよび Flyway マイグレーションの初期化
  - **対象コンポーネント & 要件**: `COMP-003`, `REQ-007`
  - **対象ファイル**: `backend/build.gradle.kts`, `backend/src/main/resources/application.yml`, `backend/src/main/resources/db/migration/V1__create_posts_table.sql`
  - **受け入れ基準**:
    - [x] Micronaut 4.x プロジェクトが Java 25 LTS、Gradle Kotlin DSL、および Micronaut Data JDBC 依存関係で構成されていること。
    - [x] Flyway マイグレーションスクリプトが `posts` テーブルを `BIGINT AUTO_INCREMENT`、UTF-8 エンコーディング、および `idx_posts_created_at_desc` インデックス付きで作成すること。
    - [x] ローカルおよびコンテナ化された MySQL データベース接続設定が検証されていること。
  - **コミットメッセージ**: `chore(backend): initialize Micronaut project and posts table migration`

- [x] **TASK-002**: PostEntity、DTO レコード、および PostRepository の実装
  - **対象コンポーネント & 要件**: `COMP-003`, `REQ-002`, `REQ-003`, `REQ-007`
  - **対象ファイル**: `backend/src/main/java/**/PostEntity.java`, `backend/src/main/java/**/dto/*.java`, `backend/src/main/java/**/PostRepository.java`, `backend/src/test/java/**/PostRepositoryTest.java`
  - **受け入れ基準**:
    - [x] `PostEntity` が自動採番 ID および作成日時とともに `posts` テーブルにマッピングされていること。
    - [x] `CreatePostRequest`, `PostResponse`, および `PagedPostResponse` の Record DTO が JSON Schema 制約に準拠して定義されていること。
    - [x] `PagedPostResponse` が `items` に対して `minItems: 0 (guaranteed [] on empty)`（空配列保証）を適用していること。
    - [x] `PostRepository` が `created_at` 降順ソートを伴う Micronaut Data `PageableRepository` を拡張していること。
    - [x] Testcontainers MySQL に対する統合テストで、リポジトリの永続化および時系列逆順ページネーションが検証されていること。
  - **コミットメッセージ**: `feat(backend): implement COMP-003 PostEntity DTOs and PostRepository`

---

### PR 2: ドメインビジネスロジック & REST コントローラー (RFC 9457)
- **ブランチ**: `feat/bulletin-board-phase2-backend-service-api`
- **マージ対象**: `feat/bulletin-board-phase1-backend-persistence`

#### タスク一覧
- [ ] **TASK-003**: PostService ドメインロジックおよびバリデーションの実装
  - **対象コンポーネント & 要件**: `COMP-002`, `REQ-002`, `REQ-003`, `REQ-007`
  - **対象ファイル**: `backend/src/main/java/**/PostService.java`, `backend/src/test/java/**/PostServiceTest.java`
  - **受け入れ基準**:
    - [ ] `PostService.getPagedPosts(page, size)` が0件時に空リスト保証を伴う `PagedPostResponse` を返却すること。
    - [ ] `PostService.createPost(command)` が非空白文字列を検証し、UTC タイムスタンプを割り当て、リポジトリの save を呼び出すこと。
    - [ ] 単体テストにより事前条件、事後条件、および不変条件の契約が 100% 分岐網羅率で検証されていること。
  - **コミットメッセージ**: `feat(backend): implement COMP-002 PostService domain logic`

- [ ] **TASK-004**: PostController および RFC 9457 エラーハンドラーの実装
  - **対象コンポーネント & 要件**: `COMP-001`, `REQ-002`, `REQ-003`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`
  - **対象ファイル**: `backend/src/main/java/**/PostController.java`, `backend/src/main/java/**/exception/*.java`, `backend/src/test/java/**/PostControllerTest.java`
  - **受け入れ基準**:
    - [ ] `GET /api/posts` がページ分割された投稿とともに `200 OK` を返却すること（デフォルト: `page=0`, `size=50`）。
    - [ ] `POST /api/posts` が Bean Validation によりボディを検証し、Location ヘッダーとともに `201 Created` を返却すること。
    - [ ] バリデーション失敗時に `application/problem+json` の RFC 9457 構造および `invalid_params` を伴う `400 Bad Request` を返却すること。
    - [ ] Micronaut HTTP クライアントテストによりコントローラーの完全なインタラクションが検証されていること。
  - **コミットメッセージ**: `feat(backend): implement COMP-001 PostController and RFC 9457 error handling`

---

### PR 3: Angular 基盤構築、フィード一覧表示 & 50件ページネーション
- **ブランチ**: `feat/bulletin-board-phase3-frontend-feed`
- **マージ対象**: `feat/bulletin-board-phase2-backend-service-api`

#### タスク一覧
- [ ] **TASK-005**: Angular プロジェクトの初期化および API クライアントサービスの実装
  - **対象コンポーネント & 要件**: `COMP-004`, `REQ-010`
  - **対象ファイル**: `frontend/package.json`, `frontend/angular.json`, `frontend/src/app/models/post.model.ts`, `frontend/src/app/services/post-api.service.ts`, `frontend/src/app/services/post-api.service.spec.ts`
  - **受け入れ基準**:
    - [ ] Angular プロジェクトが Angular Material とともに初期化・構成されていること。
    - [ ] `CreatePostRequest`, `PostResponse`, および `PagedPostResponse` に一致する TypeScript インターフェースが定義されていること。
    - [ ] `PostApiService` がエラー伝播を伴う `getPosts(page, size)` および `createPost(payload)` を処理すること。
    - [ ] `HttpClientTestingModule` を用いた `PostApiService` の単体テストが通過すること。
  - **コミットメッセージ**: `chore(frontend): initialize Angular project and implement PostApiService`

- [ ] **TASK-006**: ページネーションおよび0件表示を備えた PostFeedComponent の実装
  - **対象コンポーネント & 要件**: `COMP-004`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-010`
  - **対象ファイル**: `frontend/src/app/components/post-feed/post-feed.component.ts`, `frontend/src/app/components/post-feed/post-feed.component.html`, `frontend/src/app/components/post-feed/post-feed.component.scss`, `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **受け入れ基準**:
    - [ ] フィードが投稿者名、フォーマットされた投稿日時、タイトル、メッセージ本文、および任意の公開メールアドレスを時系列逆順で描画すること。
    - [ ] 1ページあたり50件に設定された `MatPaginator` を統合すること。
    - [ ] 総件数が0件の際にプレースホルダーメッセージを表示すること（`REQ-004`）。
    - [ ] スクロール可能なフィードコンテナが、下部フォームと重ならないように動的計算スタイルで配置されていること。
    - [ ] コンポーネントの単体テストにより描画、0件表示、およびページ遷移が検証されていること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-004 PostFeedComponent with pagination`

---

### PR 4: 画面下部固定フォーム、フィード連携 & E2E 統合検証
- **ブランチ**: `feat/bulletin-board-phase4-frontend-form-e2e`
- **マージ対象**: `feat/bulletin-board-phase3-frontend-feed`

#### タスク一覧
- [ ] **TASK-007**: 下部固定 PostFormComponent の実装およびフィード連携
  - **対象コンポーネント & 要件**: `COMP-005`, `REQ-001`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`
  - **対象ファイル**: `frontend/src/app/components/post-form/post-form.component.ts`, `frontend/src/app/components/post-form/post-form.component.html`, `frontend/src/app/components/post-form/post-form.component.scss`, `frontend/src/app/components/post-form/post-form.component.spec.ts`
  - **受け入れ基準**:
    - [ ] フォームコンテナがビューポートの最下部に固定配置されていること（`position: fixed; bottom: 0; width: 100%`）。
    - [ ] リアクティブフォームが名前（1〜50）、タイトル（1〜100）、メッセージ本文（1〜4,000）、および任意メールアドレスを検証すること。
    - [ ] バリデーション失敗時に入力値を消去せずに項目レベルのエラーメッセージを表示すること（`REQ-009`）。
    - [ ] 投稿成功時にフォームを初期化し、フィードを0ページ目に更新し、`MatSnackBar` 通知を開くこと（`REQ-008`）。
    - [ ] コンポーネントの単体テストによりフォーム状態、エラー表示、および送信コールバックが検証されていること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-005 PostFormComponent with fixed bottom layout`

- [ ] **TASK-008**: フルスタック統合スモーク検証
  - **対象コンポーネント & 要件**: `COMP-001`, `COMP-004`, `COMP-005`, `REQ-010`
  - **対象ファイル**: `README.md`, 統合スモークテストスクリプトまたはワークフロー検証
  - **受け入れ基準**:
    - [ ] エンドツーエンドのユーザーフロー（アプリ起動 -> 0件表示確認 -> メッセージ投稿 -> 完了通知および50件ページネーション付きフィード更新確認）が検証されていること。
    - [ ] バックエンドおよびフロントエンドのすべての自動単体テスト・統合テストが正常に通過すること。
  - **コミットメッセージ**: `test(e2e): verify end-to-end bulletin board flow and update documentation`

---

## 4. ライフサイクルおよびリセット規約（Lifecycle & Reset Protocol）

- **初回作成時**: すべてのタスクは未完了（`- [ ]`）として初期化されます。
- **仕様改定時**:
  - 上記のすべてのタスクが完了（`- [x]`）している場合、この計画はアーカイブ/リセットされ、改定差分のための新しいタスクリストに置き換えられます。
  - タスクが部分的に完了している場合、進行中のタスクはその場で更新され、依存関係が再調整されます。
