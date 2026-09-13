import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PostFeedComponent } from './post-feed.component';
import { PostApiService } from '../../services/post-api.service';
import { PagedPostResponse } from '../../models/post.model';

describe('PostFeedComponent (投稿一覧フィードコンポーネントテスト)', () => {
  let component: PostFeedComponent;
  let fixture: ComponentFixture<PostFeedComponent>;
  let mockPostApiService: { getPosts: ReturnType<typeof vi.fn> };

  const samplePosts: PagedPostResponse = {
    items: [
      {
        id: 10,
        name: 'Alice',
        email: 'alice@example.com',
        title: 'Latest Post Title',
        message: 'This is the newest message content.',
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

    await TestBed.configureTestingModule({
      imports: [PostFeedComponent],
      providers: [
        { provide: PostApiService, useValue: mockPostApiService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostFeedComponent);
    component = fixture.componentInstance;
  });

  it('コンポーネント初期化時(ngOnInit)に第0ページ(50件)の取得を呼び出すこと', () => {
    fixture.detectChanges();

    expect(mockPostApiService.getPosts).toHaveBeenCalledWith(0, 50);
    expect(component.posts().length).toBe(2);
    expect(component.totalItems()).toBe(2);
    expect(component.pageIndex()).toBe(0);
  });

  it('投稿が存在する場合、投稿一覧カードが正しく描画されること', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.post-card');
    expect(cards.length).toBe(2);

    // 1件目のカード（メールあり）
    const firstCard = cards[0];
    expect(firstCard.querySelector('.post-title')?.textContent).toContain('Latest Post Title');
    expect(firstCard.querySelector('.post-name')?.textContent).toContain('Alice');
    expect(firstCard.querySelector('.post-email')?.textContent).toContain('alice@example.com');
    expect(firstCard.querySelector('.post-message')?.textContent).toContain('This is the newest message content.');

    // 2件目のカード（メールなし: 要素が描画されないこと）
    const secondCard = cards[1];
    expect(secondCard.querySelector('.post-title')?.textContent).toContain('Second Post Title');
    expect(secondCard.querySelector('.post-name')?.textContent).toContain('Bob');
    expect(secondCard.querySelector('.post-email')).toBeNull();
  });

  it('レコードが0件の場合、プレースホルダーメッセージが表示されクラッシュしないこと', () => {
    mockPostApiService.getPosts.mockReturnValue(of(emptyResponse));
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const placeholder = compiled.querySelector('.empty-feed-placeholder');
    expect(placeholder).not.toBeNull();
    expect(placeholder?.textContent).toContain('投稿されたメッセージはまだありません');
    expect(compiled.querySelectorAll('.post-card').length).toBe(0);
  });

  it('ページネーションイベント発生時、指定されたページインデックスでAPIを再呼び出しすること', () => {
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

  it('refresh()呼び出し時、現在のページインデックスで再読込を行うこと', () => {
    fixture.detectChanges();
    mockPostApiService.getPosts.mockClear();
    mockPostApiService.getPosts.mockReturnValue(of(samplePosts));

    component.refresh();

    expect(mockPostApiService.getPosts).toHaveBeenCalledWith(0, 50);
  });

  it('APIエラー時、エラーメッセージが表示されローディング状態が解除されること', () => {
    mockPostApiService.getPosts.mockReturnValue(throwError(() => new Error('ネットワークエラー')));
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).not.toBeNull();

    const compiled = fixture.nativeElement as HTMLElement;
    const errorBanner = compiled.querySelector('.error-banner');
    expect(errorBanner).not.toBeNull();
  });
});
