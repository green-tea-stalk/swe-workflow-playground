# swe-workflow-playground: 掲示板アプリケーション (Bulletin Board Application)

[![GitHub Release](https://img.shields.io/github/v/release/green-tea-stalk/swe-workflow-playground)](https://github.com/green-tea-stalk/swe-workflow-playground/releases)
[![CI](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/ci.yml/badge.svg)](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/ci.yml)
[![release-please](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/release-please.yml/badge.svg)](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/release-please.yml)
[![Dependabot](https://img.shields.io/badge/dependabot-enabled-blue.svg?logo=dependabot)](https://github.com/green-tea-stalk/swe-workflow-playground/network/updates)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> [!NOTE]
> 🇺🇸 このドキュメントの英語版はこちらです: [English Version (README.md)](README.md)

`swe-workflow` プラグインの検証用プロジェクトです。仕様駆動開発 (Spec-Driven Development, SDD) に従い、Micronaut (Java 25 LTS)、MySQL 8.4 LTS、および Angular 22 (Angular Material) で構築された掲示板アプリケーションを提供します。

---

## 1. 概要

本プロジェクトは、自動化された仕様駆動開発ワークフローを検証するために構築されたフルスタック掲示板アプリケーションです。主な特徴は以下のとおりです：
- 厳密な契約プログラミング (Design by Contract, DbC) に準拠した、疎結合でレスポンシブなフルスタックアーキテクチャ。
- すべての REST API エンドポイントにおける RFC 9457 Problem Details エラーハンドリングの標準化。
- 画面下部固定フォームと、Flexbox の高さ制約を考慮した 50 件ページネーションフィード。
- 多層的な自動テストスイート：単体テスト (JUnit 5, Vitest)、契約検証テスト (Testcontainers MySQL)、および実ブラウザ E2E テスト (Playwright)。

---

## 2. 技術ドキュメント & 開発者ガイド

情報の重複を排除し、単一の真実の情報源 (Single Source of Truth) を維持するため、すべての技術詳細・実行コマンド・アーキテクチャ規約は [AGENTS.md](AGENTS.md) に集約されています。各項目については以下のセクションを参照してください：

- **システムアーキテクチャ & コンポーネント構成**: アーキテクチャ図、技術スタック、およびコンポーネント境界 (`COMP-001` 〜 `COMP-005`) については [AGENTS.md#2-system-architecture--component-inventory](AGENTS.md#2-system-architecture--component-inventory) を参照してください。
- **動作環境 & 前提条件**: JDK 25、Node.js、および Docker の要件については [AGENTS.md#3-prerequisites--environment-setup](AGENTS.md#3-prerequisites--environment-setup) を参照してください。
- **クイックスタート (起動手順)**: MySQL 8.4、Micronaut バックエンド、および Angular フロントエンドの起動コマンドについては [AGENTS.md#4-quick-start--service-execution-guide](AGENTS.md#4-quick-start--service-execution-guide) を参照してください。
- **検証 & テスト実行手順**: バックエンドテスト、フロントエンド Vitest テスト、Playwright E2E テスト、およびビルドコマンドについては [AGENTS.md#5-verification--testing-protocol](AGENTS.md#5-verification--testing-protocol) を参照してください。
- **コーディング規約 & リリース運用**: 言語規約、コメントルール、および Conventional Commits については [AGENTS.md#7-coding--linguistic-standards](AGENTS.md#7-coding--linguistic-standards) および [AGENTS.md#8-version-control--release-workflow](AGENTS.md#8-version-control--release-workflow) を参照してください。

---

## 3. 仕様ドキュメント

要件定義書、詳細設計書、およびタスク計画書（日本語版）：
- [要件定義書 (Requirements Specification)](docs/specs/bulletin-board/requirements.ja.md)
- [詳細設計書 (Design Specification)](docs/specs/bulletin-board/design.ja.md)
- [実装タスク計画書 (Task Implementation Plan)](docs/specs/bulletin-board/tasks.ja.md)
