import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { PostFeedComponent } from './post-feed.component';
import { PostApiService } from '../../services/post-api.service';
import { LocaleService, type SupportedLocale } from '../../services/locale.service';
import { PagedPostResponse } from '../../models/post.model';

describe('PostFeedComponent (Post Feed Component Unit)', () => {
  let component: PostFeedComponent;
  let fixture: ComponentFixture<PostFeedComponent>;
  let mockPostApiService: { getPosts: ReturnType<typeof vi.fn> };
  let mockLocaleService: { getActiveLocale: ReturnType<typeof vi.fn> };

  const samplePosts: PagedPostResponse = {
    items: [
      {
        id: 10,
        name: '山田 太郎',
        email: 'yamada@example.com',
        title: '多言語対応のテスト投稿',
        message: 'これは日本語の本文です。Emoji 🌸 も含まれます。',
        created_at: '2026-09-11T12:30:00Z',
      },
      {
        id: 9,
        name: 'Bob',
        email: null,
        title: 'Second Post Title',
        message: 'Second message content without email.',
        created_at: '2026-09-11T11:00:00Z',
      },
    ],
    page: 0,
    size: 50,
    total_items: 2,
    total_pages: 1,
  };

  const emptyResponse: PagedPostResponse = {
    items: [],
    page: 0,
    size: 50,
    total_items: 0,
    total_pages: 0,
  };

  beforeEach(async () => {
    mockPostApiService = {
      getPosts: vi.fn().mockReturnValue(of(samplePosts)),
    };
    mockLocaleService = {
      getActiveLocale: vi.fn().mockReturnValue('en' as SupportedLocale),
    };

    await TestBed.configureTestingModule({
      imports: [PostFeedComponent],
      providers: [
        { provide: PostApiService, useValue: mockPostApiService },
        { provide: LocaleService, useValue: mockLocaleService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostFeedComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should fetch page 0 (size 50) on component initialization (ngOnInit)', () => {
    fixture.detectChanges();

    expect(mockPostApiService.getPosts).toHaveBeenCalledWith(0, 50);
    expect(component.posts().length).toBe(2);
    expect(component.totalItems()).toBe(2);
    expect(component.pageIndex()).toBe(0);
  });

  it('should render post cards correctly and preserve user post contents unaltered', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.post-card');
    expect(cards.length).toBe(2);

    // First card (with email and multi-byte Japanese characters)
    const firstCard = cards[0];
    expect(firstCard.querySelector('.post-title')?.textContent).toContain('多言語対応のテスト投稿');
    expect(firstCard.querySelector('.post-name')?.textContent).toContain('山田 太郎');
    expect(firstCard.querySelector('.post-email')?.textContent).toContain('yamada@example.com');
    expect(firstCard.querySelector('.post-message')?.textContent).toContain('これは日本語の本文です。Emoji 🌸 も含まれます。');

    // Second card (without email: element should not be rendered)
    const secondCard = cards[1];
    expect(secondCard.querySelector('.post-title')?.textContent).toContain('Second Post Title');
    expect(secondCard.querySelector('.post-name')?.textContent).toContain('Bob');
    expect(secondCard.querySelector('.post-email')).toBeNull();
  });

  describe('precondition validation', () => {
    it.each([
      { invalidPage: -1, description: 'negative integer' },
      { invalidPage: -10, description: 'large negative integer' },
      { invalidPage: 1.5, description: 'non-integer decimal' },
      { invalidPage: Number.NaN, description: 'NaN value' },
    ])('should throw an Error when loadPage is called with $invalidPage ($description)', ({ invalidPage }) => {
      expect(() => component.loadPage(invalidPage)).toThrowError(/non-negative integer/i);
    });
  });

  describe('locale-sensitive date formatting', () => {
    it.each([
      {
        locale: 'ja' as const,
        expectedPattern: 'yyyy/MM/dd HH:mm:ss',
        expectedRegex: /^\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2}$/,
        description: 'Japanese standard timestamp format',
      },
      {
        locale: 'en' as const,
        expectedPattern: 'MMM d, y, h:mm:ss a',
        expectedRegex: /^[A-Z][a-z]{2} \d{1,2}, \d{4}, \d{1,2}:\d{2}:\d{2} [AP]M$/,
        description: 'English standard timestamp format',
      },
    ])('should format timestamps using "$expectedPattern" ($description)', ({ locale, expectedPattern, expectedRegex }) => {
      mockLocaleService.getActiveLocale.mockReturnValue(locale);
      fixture.detectChanges();

      expect(component.dateFormat).toBe(expectedPattern);

      const compiled = fixture.nativeElement as HTMLElement;
      const firstDateText = compiled.querySelector('.post-date')?.textContent?.trim() ?? '';
      expect(firstDateText).toMatch(expectedRegex);
    });
  });

  it('should display placeholder message without crashing when zero records exist', () => {
    mockPostApiService.getPosts.mockReturnValue(of(emptyResponse));
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const placeholder = compiled.querySelector('.empty-feed-placeholder');
    expect(placeholder).not.toBeNull();
    expect(placeholder?.textContent).toContain('No posts found. Be the first to post!');
    expect(compiled.querySelectorAll('.post-card').length).toBe(0);
  });

  it('should safely fall back to empty array [] when items collection is null or omitted in API response', () => {
    const nullItemsResponse = {
      items: null as unknown as [],
      page: 0,
      size: 50,
      total_items: 0,
      total_pages: 0,
    };
    mockPostApiService.getPosts.mockReturnValue(of(nullItemsResponse));
    fixture.detectChanges();

    expect(component.posts()).toEqual([]);
    expect(component.totalItems()).toBe(0);
  });

  it('should re-fetch API with specified page index upon pagination event', () => {
    fixture.detectChanges();

    const page1Response: PagedPostResponse = {
      items: [
        {
          id: 8,
          name: 'Charlie',
          email: null,
          title: 'Page 1 Post',
          message: 'Message on page 1',
          created_at: '2026-09-11T09:00:00Z',
        },
      ],
      page: 1,
      size: 50,
      total_items: 51,
      total_pages: 2,
    };
    mockPostApiService.getPosts.mockReturnValue(of(page1Response));

    component.onPageChange({ pageIndex: 1, pageSize: 50, length: 51 });
    fixture.detectChanges();

    expect(mockPostApiService.getPosts).toHaveBeenCalledWith(1, 50);
    expect(component.pageIndex()).toBe(1);
    expect(component.posts().length).toBe(1);
  });

  it('should reload with current page index when refresh() is called', () => {
    fixture.detectChanges();
    mockPostApiService.getPosts.mockClear();
    mockPostApiService.getPosts.mockReturnValue(of(samplePosts));

    component.refresh();

    expect(mockPostApiService.getPosts).toHaveBeenCalledWith(0, 50);
  });

  it('should display error message and clear loading state on API failure', () => {
    mockPostApiService.getPosts.mockReturnValue(throwError(() => new Error('Network error')));
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).toBe('Network error');

    const compiled = fixture.nativeElement as HTMLElement;
    const errorBanner = compiled.querySelector('.error-banner');
    expect(errorBanner).not.toBeNull();
  });
});
