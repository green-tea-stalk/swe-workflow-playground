import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { App } from './app';
import { PostFeedComponent } from './components/post-feed/post-feed.component';

describe('App (Application Root Integration)', () => {
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

  it('should instantiate application root component successfully', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeInstanceOf(App);
  });

  it('should display bulletin board title in toolbar', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-toolbar')?.textContent).toContain('Bulletin Board');
  });

  it('should correctly include feed component, fixed bottom form component, and language switch in toolbar', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-toolbar app-language-switch')).not.toBeNull();
    expect(compiled.querySelector('app-post-feed')).not.toBeNull();
    expect(compiled.querySelector('app-post-form')).not.toBeNull();
  });

  it('should call loadPage(0) on feed component when onPostCreated() is invoked', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    fixture.detectChanges();

    const mockFeed = { loadPage: vi.fn() } as unknown as PostFeedComponent;
    app.feedComponent = mockFeed;

    app.onPostCreated();

    expect(mockFeed.loadPage).toHaveBeenCalledWith(0);
  });
});
