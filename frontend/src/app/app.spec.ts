import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { App } from './app';
import { PostFeedComponent } from './components/post-feed/post-feed.component';
import { PostFormComponent } from './components/post-form/post-form.component';

describe('App (アプリケーションルート統合テスト)', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAnimationsAsync(),
      ],
    }).compileComponents();
  });

  it('アプリケーションルートコンポーネントが正常にインスタンス化されること', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('ツールバーに掲示板タイトルが表示されること', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-toolbar')?.textContent).toContain('掲示板アプリケーション');
  });

  it('フィードコンポーネントおよび画面下部固定フォームが正しく組み込まれていること', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-post-feed')).not.toBeNull();
    expect(compiled.querySelector('app-post-form')).not.toBeNull();
  });

  it('onPostCreated()が呼び出された際、フィードコンポーネントのloadPage(0)を呼び出して再読み込みすること', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    fixture.detectChanges();

    const mockFeed = { loadPage: vi.fn() } as unknown as PostFeedComponent;
    app.feedComponent = mockFeed;

    app.onPostCreated();

    expect(mockFeed.loadPage).toHaveBeenCalledWith(0);
  });
});
