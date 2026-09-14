---
feature: bulletin-board
document_type: tasks
version: 1.1.0
status: approved
updated_at: 2026-09-14
upstream:
  requirements: 1.1.0
  design: 1.1.0
---

# 実装タスク計画書: 掲示板アプリケーション（v1.1.0 返信機能追加）

## 1. エグゼクティブ Stacked PR 概要（Executive Stacked PR Overview）

| PR # | 対象ブランチ | フェーズ / 目的 | 主要コンポーネント | 依存先 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-v1.1-backend-persistence-domain` | バックエンド返信永続化およびドメインサービスの一括取得 | `COMP-002`, `COMP-003`, `COMP-009` | `docs/bulletin-board-v1.1-spec` | 1 |
| **PR 2** | `feat/bulletin-board-v1.1-backend-reply-api` | バックエンド返信 REST コントローラーおよび RFC 9457 例外マッピング | `COMP-001` | `PR 1` | 2 |
| **PR 3** | `feat/bulletin-board-v1.1-frontend-reply-ui` | フロントエンドモデル、API クライアント、フィード返信描画、およびフォーム返信モード | `COMP-004`, `COMP-005` | `PR 2` | 3 |
| **PR 4** | `feat/bulletin-board-v1.1-e2e-and-i18n` | 多言語メッセージ対応およびフルスタック E2E Playwright 検証 | `COMP-001`, `COMP-004`, `COMP-005` | `PR 3` | 4 |

---

## 2. 機械的トレーサビリティマトリクス（Mechanical Traceability Matrix）

すべての有効な要件および設計コンポーネントが、漏れなくマッピングされています：

| 要件 ID | コンポーネント ID | 実装タスク | 対象 PR | ステータス |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-002** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-010`, `TASK-015` | PR 1, PR 4 | 保留（Pending） |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-010`, `TASK-015` | PR 1, PR 4 | 保留（Pending） |
| **REQ-004** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-005** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-006** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-007** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005` | `TASK-010`, `TASK-014`, `TASK-015` | PR 1, PR 3, PR 4 | 保留（Pending） |
| **REQ-008** | `COMP-001`, `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-009** | `COMP-001`, `COMP-005` | `TASK-011`, `TASK-014`, `TASK-015` | PR 2, PR 3, PR 4 | 保留（Pending） |
| **REQ-010** | `COMP-001`, `COMP-004`, `COMP-005` | `TASK-011`, `TASK-013`, `TASK-014`, `TASK-015` | PR 2, PR 3, PR 4 | 保留（Pending） |
| **REQ-011** | `COMP-001`, `COMP-002`, `COMP-004`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-013`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | 保留（Pending） |
| **REQ-012** | `COMP-004`, `COMP-005` | `TASK-013`, `TASK-014`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-013** | `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | 保留（Pending） |
| **REQ-014** | `COMP-001`, `COMP-002`, `COMP-005`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-014`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | 保留（Pending） |
| **REQ-015** | `COMP-001`, `COMP-002`, `COMP-005`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-014`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | 保留（Pending） |

---

## 3. Stacked PR タスク仕様および進捗トラッカー（Progress Tracker）

実装エージェントは、**アトミックコミットループ（Atomic Commit Loop）**を用いてタスクを順次実行します：
1. 最初の未完了タスク（`- [ ]`）を選択する。
2. コードおよび通過する単体/統合テストを実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）に更新する。
4. `committing-changes` を実行し、コードと更新された `tasks.md` をアトミックにコミットする。
5. 予期せぬ中断が発生した場合は、最初の未完了タスクから直ちに再開する。

### PR 1: バックエンド返信永続化およびドメインサービスの一括取得
- **ブランチ**: `feat/bulletin-board-v1.1-backend-persistence-domain`
- **マージ対象**: `docs/bulletin-board-v1.1-spec`

#### タスク一覧
- [x] **TASK-009**: Flyway V2 マイグレーション、ReplyEntity、および ReplyRepository の実装
  - **コンポーネント & 関連要件**: `COMP-009`, `REQ-011`, `REQ-014`, `REQ-015`
  - **対象ファイル**: `backend/src/main/resources/db/migration/V2__create_replies_table.sql`, `backend/src/main/java/**/entity/ReplyEntity.java`, `backend/src/main/java/**/repository/ReplyRepository.java`, `backend/src/test/java/**/ReplyRepositoryTest.java`
  - **受け入れ基準**:
    - [x] Flyway マイグレーション `V2__create_replies_table.sql` により、`posts(id)` に対する `ON DELETE CASCADE` 外部キー、および `idx_replies_post_id`, `idx_replies_created_at_asc` インデックスを持つ `replies` テーブルが作成されること。
    - [x] `ReplyEntity` レコードが `replies` テーブルにマッピングされ、自動採番 ID とマイクロ秒タイムスタンプを持つこと。
    - [x] `ReplyRepository` インターフェースが Micronaut Data JDBC を拡張して定義され、`findByPostIdOrderByCreatedAtAsc` および `findByPostIdInOrderByCreatedAtAsc` を備えること。
    - [x] MySQL Testcontainers に対するリポジトリ統合テストにより、永続化、カスケード削除、および時系列昇順ソートが検証されること。
  - **コミットメッセージ**: `feat(backend): implement COMP-009 ReplyEntity and ReplyRepository with Flyway V2 migration`

- [ ] **TASK-010**: 返信 DTO、PostNotFoundException、および PostService 返信一括取得ロジックの実装
  - **コンポーネント & 関連要件**: `COMP-002`, `COMP-003`, `REQ-002`, `REQ-003`, `REQ-007`, `REQ-011`, `REQ-014`, `REQ-015`
  - **対象ファイル**: `backend/src/main/java/**/dto/CreateReplyRequest.java`, `backend/src/main/java/**/dto/ReplyResponse.java`, `backend/src/main/java/**/dto/PostResponse.java`, `backend/src/main/java/**/exception/PostNotFoundException.java`, `backend/src/main/java/**/service/PostService.java`, `backend/src/test/java/**/PostServiceTest.java`
  - **受け入れ基準**:
    - [ ] `CreateReplyRequest` DTO がバリデーションアノテーション付きで定義されていること（名前 1〜50文字・空白不可、メール最大254文字・任意、本文 1〜4,000文字・空白不可）。
    - [ ] `ReplyResponse` DTO が ISO 8601 UTC タイムスタンプフォーマット付きで定義されていること。
    - [ ] `PostResponse` が更新され、返信が0件の場合でも非Nullな空配列 `replies: []` を保証すること。
    - [ ] `PostService.getPagedPosts(page, size)` が `ReplyRepository.findByPostIdInOrderByCreatedAtAsc(postIds)` による単一バッチクエリを実行し、メモリ上で親投稿にマッピングして N+1 回の往復を排除していること。
    - [ ] `PostService.createReply(postId, command)` が `PostRepository.findById` により親投稿の存在を確認し、存在しない場合は `PostNotFoundException` を送出し、有効な返信を現在 UTC タイムスタンプで永続化すること。
    - [ ] 単体テストにより、事前条件、事後条件、空コレクション保証、および100%の分岐網羅率が検証されること。
  - **コミットメッセージ**: `feat(backend): implement COMP-002 PostService reply batch fetching and creation logic`

---

### PR 2: バックエンド返信 REST コントローラーおよび RFC 9457 例外マッピング
- **ブランチ**: `feat/bulletin-board-v1.1-backend-reply-api`
- **マージ対象**: `feat/bulletin-board-v1.1-backend-persistence-domain`

#### タスク一覧
- [ ] **TASK-011**: POST /api/posts/{postId}/replies エンドポイントおよび RFC 9457 エラーハンドラーの実装
  - **コンポーネント & 関連要件**: `COMP-001`, `REQ-009`, `REQ-010`, `REQ-011`, `REQ-014`, `REQ-015`
  - **対象ファイル**: `backend/src/main/java/**/controller/PostController.java`, `backend/src/main/java/**/exception/GlobalExceptionHandler.java`, `backend/src/test/java/**/PostControllerTest.java`
  - **受け入れ基準**:
    - [ ] `PostController` が `@Valid CreateReplyRequest` を受け取り、`Location` ヘッダー `/api/posts/{postId}/replies/{id}` とともに `201 Created` を返却する `POST /api/posts/{postId}/replies` を公開すること。
    - [ ] `CreateReplyRequest` のバリデーションエラー時に、詳細な `invalid_params` を含む RFC 9457 準拠の `400 Bad Request` が返却されること。
    - [ ] `PostNotFoundException` がタイプ `https://example.com/errors/post-not-found` を持つ RFC 9457 `404 Not Found` にマッピングされること。
    - [ ] 統合テストにより、返信作成の成功（201）、バリデーションエラー（400）、および親投稿不存在エラー（404）が検証されること。
  - **コミットメッセージ**: `feat(backend): implement COMP-001 reply endpoint and RFC 9457 404 error handler`

---

### PR 3: フロントエンドモデル、API クライアント、フィード返信描画、およびフォーム返信モード
- **ブランチ**: `feat/bulletin-board-v1.1-frontend-reply-ui`
- **マージ対象**: `feat/bulletin-board-v1.1-backend-reply-api`

#### タスク一覧
- [ ] **TASK-012**: TypeScript モデルおよび PostApiService の返信対応
  - **コンポーネント & 関連要件**: `COMP-004`, `COMP-005`, `REQ-011`, `REQ-014`, `REQ-015`
  - **対象ファイル**: `frontend/src/app/models/post.model.ts`, `frontend/src/app/services/post-api.service.ts`, `frontend/src/app/services/post-api.service.spec.ts`
  - **受け入れ基準**:
    - [ ] `CreateReplyRequest` および `ReplyResponse` インターフェースが `post.model.ts` に定義されていること。
    - [ ] `PostResponse` が更新され、非Nullな `readonly replies: readonly ReplyResponse[]` を保持すること。
    - [ ] `PostApiService.createReply(postId: number, request: CreateReplyRequest)` が実装され、`Observable<ReplyResponse>` を返却すること。
    - [ ] `PostApiService` の `createReply` およびエラー伝播を網羅する単体テストが通過すること。
  - **コミットメッセージ**: `feat(frontend): add reply models and PostApiService.createReply method`

- [ ] **TASK-013**: PostFeedComponent における返信一覧描画および返信ボタンの実装
  - **コンポーネント & 関連要件**: `COMP-004`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-010`, `REQ-011`, `REQ-012`
  - **対象ファイル**: `frontend/src/app/components/post-feed/post-feed.component.ts`, `frontend/src/app/components/post-feed/post-feed.component.html`, `frontend/src/app/components/post-feed/post-feed.component.scss`, `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **受け入れ基準**:
    - [ ] 各投稿カードの下部に紐づく返信一覧が投稿日時昇順で描画され、投稿者名、タイムスタンプ、本文、メールアドレスが表示されること。
    - [ ] 返信が存在しない場合、余計な余白やエラーなしに親投稿が描画されること。
    - [ ] 各投稿カードの専用「返信」アクションボタンが親/サービスに対象投稿をイベント通知すること（`REQ-012`）。
    - [ ] コンポーネント単体テストにより、返信描画、時系列順、空返信の安全性、および返信ボタンクリックイベントが検証されること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-004 reply list rendering and reply action button`

- [ ] **TASK-014**: PostFormComponent における返信モードおよび送信連携の実装
  - **コンポーネント & 関連要件**: `COMP-005`, `REQ-001`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`, `REQ-012`, `REQ-013`, `REQ-014`, `REQ-015`
  - **対象ファイル**: `frontend/src/app/components/post-form/post-form.component.ts`, `frontend/src/app/components/post-form/post-form.component.html`, `frontend/src/app/components/post-form/post-form.component.scss`, `frontend/src/app/components/post-form/post-form.component.spec.ts`, `frontend/src/app/app.ts`, `frontend/src/app/app.html`
  - **受け入れ基準**:
    - [ ] コンポーネントが `replyTarget` 状態を保持し、設定時に対象投稿識別子バッジとキャンセルボタンを表示すること（`REQ-012`, `REQ-013`）。
    - [ ] 返信モード時、タイトル入力欄が非表示となり、タイトルバリデーション制約が無効化されること（`REQ-013`）。
    - [ ] キャンセルボタン押下により `replyTarget` が解除され、タイトル欄を含む標準モードへ復帰すること（`REQ-013`）。
    - [ ] 返信モードでの送信時、`createReply` を呼び出し、フォームを初期化して標準モードへ復帰させ、フィードを再読み込みして完了スナックバーを表示すること（`REQ-014`）。
    - [ ] エラー時またはバリデーション失敗時に入力済みテキストを保持して破棄しないこと（`REQ-009`, `REQ-015`）。
    - [ ] コンポーネント単体テストにより、返信モード遷移、バリデーション、キャンセル、送信、およびエラー時の入力保持が検証されること。
  - **コミットメッセージ**: `feat(frontend): implement COMP-005 PostFormComponent reply mode and submission wiring`

---

### PR 4: 多言語メッセージ対応およびフルスタック E2E Playwright 検証
- **ブランチ**: `feat/bulletin-board-v1.1-e2e-and-i18n`
- **マージ対象**: `feat/bulletin-board-v1.1-frontend-reply-ui`

#### タスク一覧
- [ ] **TASK-015**: 多言語メッセージ追加および E2E Playwright テストの検証
  - **コンポーネント & 関連要件**: `COMP-001`, `COMP-004`, `COMP-005`, `REQ-001` 〜 `REQ-015`
  - **対象ファイル**: `frontend/src/locale/messages.xlf`, `frontend/src/locale/messages.ja.xlf`, `backend/src/main/resources/messages.properties`, `backend/src/main/resources/messages_ja.properties`, `frontend/e2e/bulletin-board.spec.ts`
  - **受け入れ基準**:
    - [ ] 新規ユーザー向け文字列（返信ボタン、返信先バッジ、キャンセルボタン、完了メッセージ、バリデーションメッセージ等）が英語および日本語で定義されていること。
    - [ ] Playwright E2E テストが完全なユーザーシナリオ（親投稿と返信の閲覧、新規投稿、返信ボタン押下、返信キャンセル、有効な返信の送信、スレッド末尾への描画確認）を網羅していること。
    - [ ] バックエンドテストスイート（`./gradlew test`）およびフロントエンドテストスイート（`npm test -- --watch=false`, `npm run e2e`）がすべて正常に通過すること。
  - **コミットメッセージ**: `test(e2e): add reply end-to-end Playwright tests and multi-locale translations`

---

## 4. ライフサイクルおよびリセット規約（Lifecycle & Reset Protocol）

- **新規作成時**: すべてのタスクは未完了（`- [ ]`）で初期化されます。
- **仕様改訂時**:
  - 旧リビジョンのタスクがすべて完了（`- [x]`）している場合、本タスク一覧はリビジョン差分に応じた新たなタスク一覧としてクリーンにリセット・再作成されます。
  - 一部タスクが進行中であった場合は、依存関係を再調整した上で既存タスクをインプレースで更新します。
