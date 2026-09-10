# swe-workflow-playground: 掲示板アプリケーション (Bulletin Board Application)

Evaluation and playground project for the `swe-workflow` plugin, demonstrating Spec-Driven Development (SDD) using Micronaut (Java 25 LTS), MySQL 8.4 LTS, and Angular 22 (Angular Material).

---

## 1. System Architecture

```mermaid
graph TD
    Browser["Web Browser (Angular 22 SPA)"]
    API["Micronaut 4.x Backend (Java 25 LTS)"]
    DB[("MySQL 8.4 LTS Database")]

    Browser -->|HTTP REST / JSON (RFC 9457)| API
    API -->|Micronaut Data JDBC / Flyway| DB
```

- **Backend**:
  - Java 25 LTS (Corretto)
  - Micronaut Framework 4.10.x
  - Micronaut Data JDBC
  - Flyway Database Migrations
  - RFC 9457 Problem Details for HTTP APIs
- **Frontend**:
  - Angular 22 (Standalone Components)
  - Angular Material & CDK
  - Vitest Test Runner
  - Viewport-anchored persistent bottom form & 50-item pagination feed
- **Database**:
  - MySQL 8.4 LTS (Optimized descending index on `created_at`)

---

## 2. Prerequisites

- **Java**: JDK 25 LTS
- **Node.js**: Node 22.x+ and npm 10.9+
- **Docker**: Docker Engine for containerized MySQL

---

## 3. Quick Start Guide

### Step 1: Start MySQL Database
Launch the MySQL 8.4 container:
```bash
docker run -d \
  --name bulletin-board-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=bulletin_board \
  -e MYSQL_USER=bbuser \
  -e MYSQL_PASSWORD=bbpassword \
  -p 3306:3306 \
  mysql:8.4
```

### Step 2: Run Backend Service
Flyway will automatically execute database migrations on startup:
```bash
cd backend
./gradlew run
```
The REST API will be available at `http://localhost:8080/api/posts`.

### Step 3: Run Frontend SPA
```bash
cd frontend
npm start
```
The web application will be accessible at `http://localhost:4200/`.

---

## 4. Verification & Testing

### Backend Test Suite
Executes 65 unit and integration tests (including Testcontainers MySQL tests and RFC 9457 contract validations):
```bash
cd backend
./gradlew test
```

### Frontend Test Suite
Executes 40 unit and integration tests via Vitest:
```bash
cd frontend
npm test -- --watch=false
```

### Production Bundle Build
```bash
cd frontend
npm run build
```

---

## 5. Specification Documentation

Comprehensive requirements, contracts, and task plans:
- [Requirements Specification (要件定義書)](docs/specs/bulletin-board/requirements.md) ([日本語版](docs/specs/bulletin-board/requirements.ja.md))
- [Design Specification (詳細設計書)](docs/specs/bulletin-board/design.md) ([日本語版](docs/specs/bulletin-board/design.ja.md))
- [Task Implementation Plan (実装タスク計画書)](docs/specs/bulletin-board/tasks.md) ([日本語版](docs/specs/bulletin-board/tasks.ja.md))

---

<details>
<summary>🇯🇵 日本語ドキュメント (Japanese Translation)</summary>

## 概要
`swe-workflow` プラグインの検証用プロジェクトです。仕様駆動開発 (Spec-Driven Development) に従い、Micronaut (Java 25 LTS)、MySQL 8.4 LTS、および Angular 22 (Angular Material) で構築された掲示板アプリケーションを提供します。

### アーキテクチャ構成
- **バックエンド**: Micronaut 4.10.x, Java 25 LTS, Micronaut Data JDBC, Flyway, RFC 9457 エラーハンドリング
- **フロントエンド**: Angular 22 (スタンドアロンコンポーネント), Angular Material, 画面下部固定フォーム, 50件ページネーションフィード
- **データベース**: MySQL 8.4 LTS (`created_at` 降順インデックス配置)

### 起動手順
1. **MySQLコンテナの起動**: `docker run` でポート 3306 に MySQL 8.4 を起動。
2. **バックエンド起動**: `cd backend && ./gradlew run` (Flywayマイグレーションが自動実行されます)。
3. **フロントエンド起動**: `cd frontend && npm start` (http://localhost:4200 にアクセス)。

### テスト実行
- **バックエンドテスト (65件通過)**: `cd backend && ./gradlew test`
- **フロントエンドテスト (40件通過)**: `cd frontend && npm test -- --watch=false`
- **プロダクションビルド**: `cd frontend && npm run build`

</details>
