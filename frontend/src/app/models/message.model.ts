/**
 * 掲示板新規メッセージ投稿リクエストモデル。
 */
export interface MessageCreateRequest {
  /** 投稿者表示名（1〜50文字、空白のみ不可） */
  name: string;
  /** 連絡先メールアドレス（任意、最大100文字） */
  email?: string | null;
  /** 投稿タイトル（1〜100文字、空白のみ不可） */
  title: string;
  /** 投稿本文（1〜1000文字、空白のみ不可） */
  message: string;
}

/**
 * 掲示板メッセージ応答リソースモデル。
 */
export interface MessageResponse {
  /** メッセージ一意識別子 */
  id: number;
  /** 投稿者表示名 */
  name: string;
  /** 連絡先メールアドレス（未設定時はnull） */
  email: string | null;
  /** 投稿タイトル */
  title: string;
  /** 投稿本文 */
  message: string;
  /** 投稿作成日時（ISO 8601 UTC形式） */
  createdAt: string;
}

/**
 * 汎用ページネーション応答封筒モデル。
 */
export interface PageResponse<T> {
  /** 現在ページのデータ配列 */
  content: T[];
  /** 0開始の現在ページインデックス */
  page: number;
  /** 1ページあたりの件数 */
  size: number;
  /** 全ページにまたがる総件数 */
  totalElements: number;
  /** 総ページ数 */
  totalPages: number;
}

/**
 * RFC 9457 個別入力検証エラー詳細モデル。
 */
export interface InvalidParam {
  /** エラーが発生した入力フィールド名 */
  name: string;
  /** エラーの理由 */
  reason: string;
}

/**
 * RFC 9457 (Problem Details for HTTP APIs) エラー応答モデル。
 */
export interface ProblemDetails {
  /** 問題タイプを特定するURI参照 */
  type: string;
  /** 人間が読める問題の概要要約 */
  title: string;
  /** HTTPステータスコード */
  status: number;
  /** 発生した問題の個別具体的な説明 */
  detail: string;
  /** 問題が発生したリソースのURI参照 */
  instance: string;
  /** 個別の入力検証エラー一覧（任意） */
  invalid_params?: InvalidParam[];
}
