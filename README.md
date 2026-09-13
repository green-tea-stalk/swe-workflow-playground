# swe-workflow-playground: Bulletin Board Application

[![GitHub Release](https://img.shields.io/github/v/release/green-tea-stalk/swe-workflow-playground)](https://github.com/green-tea-stalk/swe-workflow-playground/releases)
[![CI](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/ci.yml/badge.svg)](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/ci.yml)
[![release-please](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/release-please.yml/badge.svg)](https://github.com/green-tea-stalk/swe-workflow-playground/actions/workflows/release-please.yml)
[![Dependabot](https://img.shields.io/badge/dependabot-enabled-blue.svg?logo=dependabot)](https://github.com/green-tea-stalk/swe-workflow-playground/network/updates)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> [!NOTE]
> 🇯🇵 A Japanese version of this document is available: [日本語版 (README.ja.md)](README.ja.md)

Evaluation and playground project for the `swe-workflow` plugin, demonstrating Spec-Driven Development (SDD) using Micronaut (Java 25 LTS), MySQL 8.4 LTS, and Angular 22 (Angular Material).

---

## 1. Overview

This project provides a full-stack bulletin board application built to validate automated Spec-Driven Development (SDD) workflows. Features include:
- A decoupled, responsive full-stack architecture adhering to strict Design by Contract (DbC) interfaces.
- Standardized RFC 9457 Problem Details error responses across all API endpoints.
- Persistent viewport-anchored submission form and 50-item paginated feed with defensive scroll constraints.
- Multi-layer automated testing: Unit (JUnit 5, Vitest), Contract validation (Testcontainers MySQL), and End-to-End browser tests (Playwright).

---

## 2. Technical Documentation & Developer Guides

To maintain single-source integrity and avoid duplicate instructions, all technical details, execution commands, and architectural rules are consolidated in [AGENTS.md](AGENTS.md). Please refer to the corresponding sections:

- **System Architecture & Components**: See [AGENTS.md#2-system-architecture--component-inventory](AGENTS.md#2-system-architecture--component-inventory) for the system architecture diagram, technology stack details, and component boundaries (`COMP-001` through `COMP-005`).
- **Prerequisites & Setup**: See [AGENTS.md#3-prerequisites--environment-setup](AGENTS.md#3-prerequisites--environment-setup) for JDK 25, Node.js, and Docker requirements.
- **Quick Start Guide**: See [AGENTS.md#4-quick-start--service-execution-guide](AGENTS.md#4-quick-start--service-execution-guide) for step-by-step commands to launch MySQL 8.4, the Micronaut backend service, and the Angular frontend application.
- **Verification & Testing Protocol**: See [AGENTS.md#5-verification--testing-protocol](AGENTS.md#5-verification--testing-protocol) for commands to execute backend tests, frontend Vitest tests, Playwright E2E suites, and production bundle builds.
- **Coding & Release Standards**: See [AGENTS.md#7-coding--linguistic-standards](AGENTS.md#7-coding--linguistic-standards) and [AGENTS.md#8-version-control--release-workflow](AGENTS.md#8-version-control--release-workflow) for language conventions, comment rules, and Conventional Commits.

---

## 3. Specification Documentation

Comprehensive requirements, contracts, and task plans (English canonical versions):
- [Requirements Specification](docs/specs/bulletin-board/requirements.md)
- [Design Specification](docs/specs/bulletin-board/design.md)
- [Task Implementation Plan](docs/specs/bulletin-board/tasks.md)
