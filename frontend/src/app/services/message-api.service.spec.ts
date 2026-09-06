import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MessageApiService } from './message-api.service';
import { MessageCreateRequest, MessageResponse, PageResponse, ProblemDetails } from '../models/message.model';

describe('MessageApiService の単体テスト', () => {
  let service: MessageApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MessageApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(MessageApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('サービスが正しくインスタンス化されること', () => {
    expect(service).toBeTruthy();
  });

  describe('getMessages メソッドの検証', () => {
    it('指定したページ番号と件数のクエリパラメータで GET リクエストを送信し、一覧を取得できること', () => {
      const mockResponse: PageResponse<MessageResponse> = {
        content: [
          {
            id: 1,
            name: '山田 太郎',
            email: 'yamada@example.com',
            title: 'テストタイトル',
            message: 'テスト本文',
            createdAt: '2026-09-06T12:00:00Z'
          }
        ],
        page: 0,
        size: 50,
        totalElements: 1,
        totalPages: 1
      };

      service.getMessages(0, 50).subscribe((res) => {
        expect(res).toEqual(mockResponse);
        expect(res.content.length).toBe(1);
        expect(res.content[0].name).toBe('山田 太郎');
      });

      const req = httpMock.expectOne((request) => {
        return (
          request.url.includes('/api/messages') &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '50'
        );
      });

      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('引数を省略した場合、デフォルトで page=0, size=50 が設定されてリクエストされること', () => {
      service.getMessages().subscribe();

      const req = httpMock.expectOne((request) => {
        return (
          request.url.includes('/api/messages') &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '50'
        );
      });

      expect(req.request.method).toBe('GET');
      req.flush({ content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 });
    });
  });

  describe('postMessage メソッドの検証', () => {
    it('新規メッセージ投稿リクエストを POST し、作成されたメッセージが返却されること', () => {
      const newRequest: MessageCreateRequest = {
        name: '山田 太郎',
        email: 'yamada@example.com',
        title: '投稿タイトル',
        message: '投稿本文'
      };

      const mockCreatedResponse: MessageResponse = {
        id: 10,
        name: '山田 太郎',
        email: 'yamada@example.com',
        title: '投稿タイトル',
        message: '投稿本文',
        createdAt: '2026-09-06T12:05:00Z'
      };

      service.postMessage(newRequest).subscribe((res) => {
        expect(res).toEqual(mockCreatedResponse);
        expect(res.id).toBe(10);
      });

      const req = httpMock.expectOne((request) => request.url.includes('/api/messages'));
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newRequest);
      req.flush(mockCreatedResponse, { status: 201, statusText: 'Created' });
    });

    it('バリデーションエラー時に 400 ProblemDetails エラーが伝播されること', () => {
      const invalidRequest: MessageCreateRequest = {
        name: '',
        title: '',
        message: ''
      };

      const mockProblemDetails: ProblemDetails = {
        type: 'https://api.bulletin-board.local/errors/invalid-request',
        title: 'Bad Request',
        status: 400,
        detail: '入力値検証に失敗しました',
        instance: '/api/messages',
        invalid_params: [
          { name: 'name', reason: '名前は必須です' }
        ]
      };

      service.postMessage(invalidRequest).subscribe({
        next: () => fail('エラーが発生するべきです'),
        error: (error) => {
          expect(error.status).toBe(400);
          expect(error.error).toEqual(mockProblemDetails);
        }
      });

      const req = httpMock.expectOne((request) => request.url.includes('/api/messages'));
      expect(req.request.method).toBe('POST');
      req.flush(mockProblemDetails, { status: 400, statusText: 'Bad Request' });
    });
  });
});
