import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { PostApiService } from './post-api.service';
import { LocaleService, type SupportedLocale } from './locale.service';
import { CreatePostRequest, PagedPostResponse, PostResponse, ProblemDetails } from '../models/post.model';

describe('PostApiService (Bulletin Board API Service Unit)', () => {
  let service: PostApiService;
  let httpMock: HttpTestingController;
  let mockLocaleService: {
    getActiveLocale: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockLocaleService = {
      getActiveLocale: vi.fn().mockReturnValue('en' as SupportedLocale),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PostApiService,
        { provide: LocaleService, useValue: mockLocaleService },
      ],
    });
    service = TestBed.inject(PostApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('should send GET /api/posts request with default parameters (page=0, size=50)', () => {
    const mockResponse: PagedPostResponse = {
      items: [
        {
          id: 1,
          name: 'Alice',
          email: 'alice@example.com',
          title: 'Title',
          message: 'Hello',
          created_at: '2026-09-11T10:00:00Z',
        },
      ],
      page: 0,
      size: 50,
      total_items: 1,
      total_pages: 1,
    };

    service.getPosts().subscribe((response) => {
      expect(response).toEqual(mockResponse);
      expect(response.items.length).toBe(1);
      expect(response.items[0].name).toBe('Alice');
    });

    const req = httpMock.expectOne('/api/posts?page=0&size=50');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Accept-Language')).toBe('en');
    req.flush(mockResponse);
  });

  it('should append custom query parameters correctly (page=2, size=20)', () => {
    service.getPosts(2, 20).subscribe((response) => {
      expect(response.page).toBe(2);
      expect(response.size).toBe(20);
      expect(response.items).toEqual([]);
    });

    const req = httpMock.expectOne('/api/posts?page=2&size=20');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Accept-Language')).toBe('en');
    req.flush({ items: [], page: 2, size: 20, total_items: 0, total_pages: 0 });
  });

  it('should safely receive empty array [] when zero records exist', () => {
    const emptyResponse: PagedPostResponse = {
      items: [],
      page: 0,
      size: 50,
      total_items: 0,
      total_pages: 0,
    };

    service.getPosts(0, 50).subscribe((response) => {
      expect(response.items).toBeDefined();
      expect(response.items.length).toBe(0);
      expect(response.total_items).toBe(0);
    });

    const req = httpMock.expectOne('/api/posts?page=0&size=50');
    expect(req.request.headers.get('Accept-Language')).toBe('en');
    req.flush(emptyResponse);
  });

  it('getPosts: should safely re-throw HttpErrorResponse upon backend error response', () => {
    const problemDetails: ProblemDetails = {
      type: 'https://example.com/errors/internal-server-error',
      title: 'Internal Server Error',
      status: 500,
      detail: 'An unexpected error occurred.',
      instance: '/api/posts',
      invalid_params: [],
    };

    service.getPosts().subscribe({
      next: () => expect.fail('Expected error callback to be invoked'),
      error: (error: unknown) => {
        expect(error).toBeInstanceOf(HttpErrorResponse);
        const httpError = error as HttpErrorResponse;
        expect(httpError.status).toBe(500);
        expect(httpError.error).toEqual(problemDetails);
      },
    });

    const req = httpMock.expectOne('/api/posts?page=0&size=50');
    req.flush(problemDetails, { status: 500, statusText: 'Internal Server Error' });
  });

  it('createPost: should send valid payload to POST /api/posts and receive created PostResponse', () => {
    const requestPayload: CreatePostRequest = {
      name: 'Bob',
      email: 'bob@example.com',
      title: 'New Post',
      message: 'Post body content',
    };

    const mockCreatedResponse: PostResponse = {
      id: 42,
      name: 'Bob',
      email: 'bob@example.com',
      title: 'New Post',
      message: 'Post body content',
      created_at: '2026-09-11T12:00:00Z',
    };

    service.createPost(requestPayload).subscribe((response) => {
      expect(response).toEqual(mockCreatedResponse);
      expect(response.id).toBe(42);
      expect(response.created_at).toBe('2026-09-11T12:00:00Z');
    });

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Accept-Language')).toBe('en');
    expect(req.request.body).toEqual(requestPayload);
    req.flush(mockCreatedResponse, { status: 201, statusText: 'Created' });
  });

  it('createPost: should preserve multi-byte UTF-8 characters in request payload without corruption', () => {
    const utf8Payload: CreatePostRequest = {
      name: '山田 太郎',
      title: '多言語対応テスト',
      message: 'こんにちは、世界！マルチバイト文字（日本語・絵文字 🌸）の送信テストです。',
    };

    const mockUtf8Response: PostResponse = {
      id: 99,
      name: '山田 太郎',
      email: null,
      title: '多言語対応テスト',
      message: 'こんにちは、世界！マルチバイト文字（日本語・絵文字 🌸）の送信テストです。',
      created_at: '2026-09-13T12:00:00Z',
    };

    service.createPost(utf8Payload).subscribe((response) => {
      expect(response).toEqual(mockUtf8Response);
      expect(response.name).toBe('山田 太郎');
      expect(response.message).toContain('🌸');
    });

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(utf8Payload);
    req.flush(mockUtf8Response);
  });

  it('createPost: should receive 400 Bad Request and RFC 9457 ProblemDetails upon validation failure', () => {
    const invalidPayload: CreatePostRequest = {
      name: '',
      title: '',
      message: '',
    };

    const problemDetails: ProblemDetails = {
      type: 'https://example.com/errors/validation-failed',
      title: 'Validation Failed',
      status: 400,
      detail: 'Input payload failed validation constraints.',
      instance: '/api/posts',
      invalid_params: [
        { name: 'name', reason: 'Name must not be blank' },
      ],
    };

    service.createPost(invalidPayload).subscribe({
      next: () => expect.fail('Error callback should have been called for error response'),
      error: (error: unknown) => {
        expect(error).toBeInstanceOf(HttpErrorResponse);
        const httpError = error as HttpErrorResponse;
        expect(httpError.status).toBe(400);
        expect(httpError.error).toEqual(problemDetails);
      },
    });

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.headers.get('Accept-Language')).toBe('en');
    req.flush(problemDetails, { status: 400, statusText: 'Bad Request' });
  });

  describe('Accept-Language header injection', () => {
    it.each([
      { locale: 'en' as const },
      { locale: 'ja' as const },
    ])('should attach "Accept-Language: $locale" header to GET /api/posts calls', ({ locale }) => {
      mockLocaleService.getActiveLocale.mockReturnValue(locale);

      service.getPosts().subscribe();

      const req = httpMock.expectOne('/api/posts?page=0&size=50');
      expect(req.request.headers.has('Accept-Language')).toBe(true);
      expect(req.request.headers.get('Accept-Language')).toBe(locale);
      req.flush({ items: [], page: 0, size: 50, total_items: 0, total_pages: 0 });
    });

    it.each([
      { locale: 'en' as const },
      { locale: 'ja' as const },
    ])('should attach "Accept-Language: $locale" header to POST /api/posts calls', ({ locale }) => {
      mockLocaleService.getActiveLocale.mockReturnValue(locale);

      const requestPayload: CreatePostRequest = {
        name: 'Tester',
        title: 'Title',
        message: 'Message',
      };

      service.createPost(requestPayload).subscribe();

      const req = httpMock.expectOne('/api/posts');
      expect(req.request.headers.has('Accept-Language')).toBe(true);
      expect(req.request.headers.get('Accept-Language')).toBe(locale);
      req.flush({
        id: 1,
        name: 'Tester',
        title: 'Title',
        message: 'Message',
        created_at: '2026-09-13T12:00:00Z',
      });
    });
  });
});
