import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageCreateRequest, MessageResponse, PageResponse } from '../models/message.model';

/**
 * 掲示板バックエンド REST API との通信をカプセル化するクライアントサービス。
 * ステートレスなシングルトンとして振る舞い、イミュータブルなリクエストパラメータを構築して通信を行う。
 */
@Injectable({
  providedIn: 'root'
})
export class MessageApiService {
  private readonly http = inject(HttpClient);
  readonly apiUrl = 'http://localhost:8080/api/messages';

  /**
   * 指定されたページ番号および件数に従い、メッセージ一覧を取得する。
   *
   * @param page 0開始のページインデックス（0以上、デフォルト: 0）
   * @param size 1ページあたりの件数（1以上、デフォルト: 50）
   * @returns ページネーションされたメッセージ応答の Observable
   */
  getMessages(page: number = 0, size: number = 50): Observable<PageResponse<MessageResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<MessageResponse>>(this.apiUrl, { params });
  }

  /**
   * 新規メッセージを投稿・作成する。
   *
   * @param request 投稿リクエスト（名前・タイトル・本文必須、メール任意）
   * @returns 永続化されたメッセージ応答の Observable
   */
  postMessage(request: MessageCreateRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.apiUrl, request);
  }
}
