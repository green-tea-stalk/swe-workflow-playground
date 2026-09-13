---
feature: i18n-support
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# 実装タスク計画書: 国際化対応（i18n）

本文書は、掲示板アプリケーション全体にわたる二言語対応（`ja` および `en`）の国際化（i18n: Internationalization）を実現するための実装タスク計画、Stacked PR の境界、受け入れ基準、および機械的トレーサビリティマトリクスを定義します。

---

## 1. Stacked PR 概要（Executive Stacked PR Overview）

| PR # | 対象ブランチ | フェーズ / 目的 | 主要コンポーネント | 依存関係 | マージ順序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/i18n-phase1-backend-localization` | バックエンドメッセージ多言語化 & RFC 9457 コンテンツネゴシエーション | `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `docs/i18n-support-spec` | 1 |
| **PR 2** | `feat/i18n-phase2-frontend-foundation` | フロントエンド i18n ツールチェーン、ロケールサービス & ナビゲーション | `COMP-101`, `COMP-102`, `COMP-105` | `feat/i18n-phase1-backend-localization` | 2 |
| **PR 3** | `feat/i18n-phase3-views-and-e2e` | ローカライズ済み UI ビュー、XLIFF バンドル & Playwright E2E スイート | `COMP-103`, `COMP-104` | `feat/i18n-phase2-frontend-foundation` | 3 |

---

## 2. 機械的トレーサビリティマトリクス（Mechanical Traceability Matrix）

すべてのアクティブな要件および設計コンポーネントは、漏れなくマッピングされています。

| 要件 ID | コンポーネント ID | 実装タスク | 対象 PR | 状態 |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-101`, `COMP-103`, `COMP-104`, `COMP-202` | `TASK-001`, `TASK-004`, `TASK-006`, `TASK-008`, `TASK-009`, `TASK-010`, `TASK-011` | PR 1, PR 2, PR 3 | 完了 (Completed) |
| **REQ-002** | `COMP-102`, `COMP-201` | `TASK-002`, `TASK-004`, `TASK-005`, `TASK-011` | PR 1, PR 2, PR 3 | 完了 (Completed) |
| **REQ-003** | `COMP-101` | `TASK-006`, `TASK-011` | PR 2, PR 3 | 完了 (Completed) |
| **REQ-004** | `COMP-102` | `TASK-005`, `TASK-011` | PR 2, PR 3 | 完了 (Completed) |
| **REQ-005** | `COMP-102` | `TASK-005`, `TASK-011` | PR 2, PR 3 | 完了 (Completed) |
| **REQ-006** | `COMP-102`, `COMP-103` | `TASK-005`, `TASK-008`, `TASK-011` | PR 2, PR 3 | 完了 (Completed) |
| **REQ-007** | `COMP-103` | `TASK-008`, `TASK-011` | PR 3 | 完了 (Completed) |
| **REQ-008** | `COMP-104`, `COMP-203`, `COMP-204` | `TASK-003`, `TASK-009`, `TASK-011` | PR 1, PR 3 | 完了 (Completed) |
| **REQ-009** | `COMP-105`, `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `TASK-001`, `TASK-002`, `TASK-003`, `TASK-007`, `TASK-011` | PR 1, PR 2, PR 3 | 完了 (Completed) |
| **REQ-010** | `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `TASK-001`, `TASK-002`, `TASK-003`, `TASK-011` | PR 1, PR 3 | 完了 (Completed) |
| **REQ-011** | `COMP-103`, `COMP-104` | `TASK-008`, `TASK-009`, `TASK-011` | PR 3 | 完了 (Completed) |

---

## 3. Stacked PR タスク仕様および進捗トラッカー（Stacked PR Task Specifications & Progress Tracker）

実装エージェントは、**アトミックコミットループ（Atomic Commit Loop）**を使用してタスクを順次実行します。
1. 未完了の先頭タスク（`- [ ]`）を選択する。
2. 厳格な TDD に従ってコードと合格する単体/統合テストを実装する。
3. タスクおよび受け入れ基準のチェックボックスを完了（`- [x]`）にする。
4. `committing-changes` を実行し、コードと更新された `tasks.md` をアトミックにコミットする。
5. 予期せぬ中断が発生した場合は、未完了の先頭タスクから直ちに再開する。

---

### PR 1: バックエンドメッセージ多言語化 & RFC 9457 コンテンツネゴシエーション
- **ブランチ**: `feat/i18n-phase1-backend-localization`
- **マージ対象**: `docs/i18n-support-spec`

#### タスク一覧
- [x] **TASK-001**: `MessageLocalizationService` の実装およびリソースバンドルの作成
  - **コンポーネント & 要件**: `COMP-202`, `REQ-001`, `REQ-009`, `REQ-010`
  - **対象ファイル**:
    - `backend/src/main/resources/messages.properties`
    - `backend/src/main/resources/messages_ja.properties`
    - `backend/src/main/java/com/example/bulletinboard/service/MessageLocalizationService.java`
    - `backend/src/main/java/com/example/bulletinboard/service/DefaultMessageLocalizationService.java`
    - `backend/src/test/java/com/example/bulletinboard/service/MessageLocalizationServiceTest.java`
  - **受け入れ基準**:
    - [x] `messages.properties`（英語）および `messages_ja.properties`（日本語）が設計書セクション 3.3 に一致する同一のキー階層を定義していること。
    - [x] `MessageLocalizationService` がコードと `Locale` に基づいてメッセージを解決し、英語へフォールバックできること。
    - [x] 存在しないキーに対して未処理例外をスローせず、安全にデフォルトメッセージを返却すること。
    - [x] 単体テストでパラメータ展開、日本語検索、および英語フォールバックを検証していること。
  - **コミットメッセージ**: `feat(backend): implement MessageLocalizationService and resource bundles`

- [x] **TASK-002**: HTTP `Accept-Language` ヘッダー解析用 `LocaleResolver` の実装
  - **コンポーネント & 要件**: `COMP-201`, `REQ-002`, `REQ-009`, `REQ-010`
  - **対象ファイル**:
    - `backend/src/main/java/com/example/bulletinboard/util/LocaleResolver.java`
    - `backend/src/main/java/com/example/bulletinboard/util/HttpLocaleResolver.java`
    - `backend/src/test/java/com/example/bulletinboard/util/LocaleResolverTest.java`
  - **受け入れ基準**:
    - [x] `ja` および `ja-*` タグを `Locale.JAPANESE` に解決すること。
    - [x] `en` および `en-*` タグを `Locale.ENGLISH` に解決すること。
    - [x] ヘッダーの欠落、空文字、ワイルドカード、または未サポートの言語タグをデフォルトの `Locale.ENGLISH` に解決すること。
    - [x] 単体テストでパラメータ化されたヘッダー解析（`q` 値、大文字小文字の区別なし、空文字列）を検証していること。
  - **コミットメッセージ**: `feat(backend): implement HttpLocaleResolver for Accept-Language negotiation`

- [x] **TASK-003**: RFC 9457 例外ハンドラーおよびバリデーションエンベロープのローカライズ
  - **コンポーネント & 要件**: `COMP-203`, `COMP-204`, `REQ-008`, `REQ-009`, `REQ-010`
  - **対象ファイル**:
    - `backend/src/main/java/com/example/bulletinboard/exception/ValidationExceptionHandler.java`
    - `backend/src/main/java/com/example/bulletinboard/exception/GlobalExceptionHandler.java`
    - `backend/src/main/java/com/example/bulletinboard/exception/IllegalArgumentExceptionHandler.java`
    - `backend/src/test/java/com/example/bulletinboard/controller/PostControllerTest.java`
    - `backend/src/test/java/com/example/bulletinboard/exception/ExceptionHandlerTest.java`
  - **受け入れ基準**:
    - [x] `ValidationExceptionHandler` がクライアントロケールに一致したローカライズ済み `title`, `detail`, `invalid_params[].reason` を含む Problem Details を生成すること。
    - [x] `GlobalExceptionHandler` および `IllegalArgumentExceptionHandler` がローカライズされたエンベロープを生成すること。
    - [x] 統合テストで、`Accept-Language: ja` 送信時に `POST /api/posts` が日本語の Problem Details を返し、`Accept-Language: en` またはヘッダーなし時に英語を返すことを検証していること。
    - [x] 違反が0件の場合、`invalid_params` が非 null の空配列 `[]` であることを保証すること。
  - **コミットメッセージ**: `feat(backend): localize RFC 9457 exception handlers with Accept-Language support`

---

### PR 2: フロントエンド多言語化基盤 & ロケール管理
- **ブランチ**: `feat/i18n-phase2-frontend-foundation`
- **マージ対象**: `feat/i18n-phase1-backend-localization`

#### タスク一覧
- [x] **TASK-004**: `@angular/localize` の設定および複数ロケールビルド構成の構築
  - **コンポーネント & 要件**: `COMP-101`, `COMP-102`, `REQ-001`, `REQ-002`
  - **対象ファイル**:
    - `frontend/package.json`
    - `frontend/angular.json`
    - `frontend/src/locale/messages.ja.xlf`
  - **受け入れ基準**:
    - [x] プロジェクトの依存関係に `@angular/localize` が追加されていること。
    - [x] `angular.json` に `i18n` の sourceLocale `en` および `src/locale/messages.ja.xlf` を指すロケール `ja` が設定されていること。
    - [x] ビルドターゲットに Base Href（`/en/` および `/ja/`）が設定されていること。
    - [x] プロダクションビルドが正常に完了し、複数ロケールの配布パッケージが生成されること。
  - **コミットメッセージ**: `build(frontend): configure @angular/localize and multi-locale build targets`

- [x] **TASK-005**: 保存・検出・遷移用 `LocaleService` の実装
  - **コンポーネント & 要件**: `COMP-102`, `REQ-002`, `REQ-004`, `REQ-005`, `REQ-006`
  - **対象ファイル**:
    - `frontend/src/app/services/locale.service.ts`
    - `frontend/src/app/services/locale.service.spec.ts`
  - **受け入れ基準**:
    - [x] `getActiveLocale()` がドキュメントの Base Href またはロケーションパスからアクティブなロケールを解決すること。
    - [x] `getStoredLocale()` が `localStorage` から `bb_locale` を読み取り検証すること。
    - [x] `resolveInitialLocale()` が `localStorage` を評価し、`navigator.language.startsWith('ja') ? 'ja' : 'en'` へフォールバックすること。
    - [x] `setLocale(target)` が `localStorage` に設定を保存し、ロケーションを `'/' + target + '/'` へ遷移させること。
    - [x] Vitest 単体テストで、モック化したストレージおよびウィンドウロケーションを用いて 100% のブランチカバレッジを達成すること。
  - **コミットメッセージ**: `feat(frontend): implement LocaleService for discovery, persistence, and navigation`

- [x] **TASK-006**: ツールバー内の `LanguageSwitchComponent` の実装
  - **コンポーネント & 要件**: `COMP-101`, `REQ-001`, `REQ-003`
  - **対象ファイル**:
    - `frontend/src/app/components/language-switch/language-switch.component.ts`
    - `frontend/src/app/components/language-switch/language-switch.component.html`
    - `frontend/src/app/components/language-switch/language-switch.component.scss`
    - `frontend/src/app/components/language-switch/language-switch.component.spec.ts`
    - `frontend/src/app/app.ts`
    - `frontend/src/app/app.html`
    - `frontend/src/app/app.spec.ts`
  - **受け入れ基準**:
    - [x] ツールバー内に現在のロケール（`EN` または `JA`）を表示する言語切替トグルボタンを描画すること。
    - [x] 切替ボタンの活性化により、遷移先ロケールを指定して `LocaleService.setLocale()` を呼び出すこと。
    - [x] ボタンにローカライズされたアクセシブルなラベル（`aria-label`）が含まれていること。
    - [x] 単体テストでクリック操作およびサービスの委譲を検証していること。
  - **コミットメッセージ**: `feat(frontend): implement LanguageSwitchComponent in navigation toolbar`

- [x] **TASK-007**: `PostApiService` における `Accept-Language` ヘッダーの付与
  - **コンポーネント & 要件**: `COMP-105`, `REQ-009`
  - **対象ファイル**:
    - `frontend/src/app/services/post-api.service.ts`
    - `frontend/src/app/services/post-api.service.spec.ts`
  - **受け入れ基準**:
    - [x] すべての HTTP GET および POST リクエストに、`LocaleService.getActiveLocale()` に一致する `Accept-Language: {locale}` を付与すること。
    - [x] Vitest 単体テストで、`HttpTestingController` を使用して `'en'` および `'ja'` 両方の送信リクエストヘッダーを検査・検証すること。
  - **コミットメッセージ**: `feat(frontend): inject Accept-Language header in PostApiService calls`

---

### PR 3: フロントエンドビュー多言語化 & Playwright E2E 検証
- **ブランチ**: `feat/i18n-phase3-views-and-e2e`
- **マージ対象**: `feat/i18n-phase2-frontend-foundation`

#### タスク一覧
- [x] **TASK-008**: `PostFeedComponent` の多言語化およびロケール対応日時フォーマットの設定
  - **コンポーネント & 要件**: `COMP-103`, `REQ-001`, `REQ-006`, `REQ-007`, `REQ-011`
  - **対象ファイル**:
    - `frontend/src/app/components/post-feed/post-feed.component.html`
    - `frontend/src/app/components/post-feed/post-feed.component.ts`
    - `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **受け入れ基準**:
    - [x] フィードテンプレートのテキスト（空表示プレースホルダー、エラーバナー、再試行ボタン、ページネーターラベル）に `i18n` 属性が付与されていること。
    - [x] 投稿日時が `DatePipe` を使用して `ja` では `yyyy/MM/dd HH:mm:ss`、`en` では `MMM d, y, h:mm:ss a` でフォーマットされること。
    - [x] ユーザーの投稿内容（`name`, `title`, `message`）が翻訳されずに元の形式で保持されること。
    - [x] 単体テストで両ロケールでのフィード描画を検証していること。
  - **コミットメッセージ**: `feat(frontend): localize PostFeedComponent and configure DatePipe locale formatting`

- [x] **TASK-009**: `PostFormComponent` のラベル、プレースホルダー、およびエラーメッセージの多言語化
  - **コンポーネント & 要件**: `COMP-104`, `REQ-001`, `REQ-008`, `REQ-011`
  - **対象ファイル**:
    - `frontend/src/app/components/post-form/post-form.component.html`
    - `frontend/src/app/components/post-form/post-form.component.ts`
    - `frontend/src/app/components/post-form/post-form.component.spec.ts`
  - **受け入れ基準**:
    - [x] フォームラベル、入力プレースホルダー、インラインバリデーションエラー、および送信ボタンに `i18n` 属性が付与されていること。
    - [x] 成功および失敗のスナックバー通知でローカライズされたテキストが使用されていること。
    - [x] エラースナックバーにローカライズされた RFC 9457 の `detail` が表示されること。
    - [x] 単体テストで英語および日本語のフォーム操作とバリデーションエラーを検証していること。
  - **コミットメッセージ**: `feat(frontend): localize PostFormComponent inputs, errors, and snackbars`

- [x] **TASK-010**: XLIFF 翻訳カタログの抽出および生成
  - **コンポーネント & 要件**: `COMP-101`, `COMP-103`, `COMP-104`, `REQ-001`
  - **対象ファイル**:
    - `frontend/src/locale/messages.ja.xlf`
    - `frontend/src/locale/messages.xlf`
  - **受け入れ基準**:
    - [x] `ng extract-i18n` を実行して元の翻訳カタログを生成すること。
    - [x] すべてのテンプレート `i18n` ID を網羅した完全な日本語翻訳が `messages.ja.xlf` に格納されていること。
    - [x] プロダクションビルド（`npm run build`）が、翻訳漏れ警告なしに `en` および `ja` 配布パッケージの両方で正常に成功すること。
  - **コミットメッセージ**: `i18n(frontend): populate Japanese XLIFF translation catalog`

- [x] **TASK-011**: 二言語検証用 Playwright E2E 自動テストスイートの構築
  - **コンポーネント & 要件**: `COMP-101`, `COMP-102`, `COMP-103`, `COMP-104`, `COMP-105`, `REQ-001` 〜 `REQ-011`
  - **対象ファイル**:
    - `frontend/e2e/i18n-language-switch.spec.ts`
  - **受け入れ基準**:
    - [x] シナリオ 1: 初回アクセスで言語検出（ブラウザ言語の一致）を検証すること。
    - [x] シナリオ 2: ツールバーの言語切り替えで UI が切り替わり、日時フォーマットが変更され、`localStorage` に `bb_locale` が保存されることを検証すること。
    - [x] シナリオ 3: フォームバリデーションエラーおよび送信フィードバックがアクティブな言語で表示されることを検証すること。
    - [x] シナリオ 4: ユーザーが投稿したメッセージが両方のロケールで変更されずに表示されることを検証すること。
    - [x] すべての Playwright E2E テストがヘッドレス Chromium で正常に合格すること。
  - **コミットメッセージ**: `test(e2e): add Playwright automated test suite for bilingual i18n workflows`

---

## 4. ライフサイクルおよびリセット規約（Lifecycle & Reset Protocol）

- **初回作成時**: すべてのタスクは `- [ ]` で初期化されます。
- **仕様改訂時**:
  - 上記のすべてのタスクが完了（`- [x]`）している場合、本計画はアーカイブ/リセットされ、改訂差分に応じた新しいタスクリストに更新されます。
  - タスクが部分的に完了している場合、依存関係を再調整した上でアクティブなタスクがインプレースで更新されます。
