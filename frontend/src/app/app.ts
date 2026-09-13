import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { LanguageSwitchComponent } from './components/language-switch/language-switch.component';
import { PostFeedComponent } from './components/post-feed/post-feed.component';
import { PostFormComponent } from './components/post-form/post-form.component';
import { LocaleService } from './services/locale.service';

/**
 * Root application component coordinating navigation header, message feed, and persistent bottom form.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MatToolbarModule, LanguageSwitchComponent, PostFeedComponent, PostFormComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly localeService = inject(LocaleService);
  private readonly document = inject(DOCUMENT);

  /** Reference to the feed child component for triggering refreshes. */
  @ViewChild(PostFeedComponent) feedComponent?: PostFeedComponent;

  /**
   * Evaluates initial locale on root application access and routes to matching locale distribution.
   */
  ngOnInit(): void {
    const pathname = this.document.location?.pathname ?? '';
    if (pathname === '/' || pathname === '') {
      const initial = this.localeService.resolveInitialLocale();
      if (initial !== this.localeService.getActiveLocale()) {
        this.localeService.setLocale(initial);
      }
    }
  }

  /**
   * Refreshes the post feed back to page 0 upon successful post creation.
   */
  onPostCreated(): void {
    this.feedComponent?.loadPage(0);
  }
}
