import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PostApiService } from './post-api.service';
import { CreatePostRequest, PagedPostResponse, PostResponse, ProblemDetails } from '../models/post.model';

describe('PostApiService (Bulletin Board API Service Unit)', () => {
  let service: PostApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PostApiService,
      ],
    });
    service = TestBed.inject(PostApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
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
    req.flush(mockResponse);
  });

  it('should append custom query parameters correctly (page=2, size=20)', () => {
    service.getPosts(2, 20).subscribe();

    const req = httpMock.expectOne('/api/posts?page=2&size=20');
    expect(req.request.method).toBe('GET');
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
    req.flush(emptyResponse);
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
    expect(req.request.body).toEqual(requestPayload);
    req.flush(mockCreatedResponse, { status: 201, statusText: 'Created' });
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
      error: (error) => {
        expect(error.status).toBe(400);
        expect(error.error).toEqual(problemDetails);
      },
    });

    const req = httpMock.expectOne('/api/posts');
    req.flush(problemDetails, { status: 400, statusText: 'Bad Request' });
  });
});
