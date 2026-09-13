import { Component, ViewChild } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { LanguageSwitchComponent } from './components/language-switch/language-switch.component';
import { PostFeedComponent } from './components/post-feed/post-feed.component';
import { PostFormComponent } from './components/post-form/post-form.component';

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
export class App {
  /** Application display title. */
  readonly title = '掲示板アプリケーション';

  /** Reference to the feed child component for triggering refreshes. */
  @ViewChild(PostFeedComponent) feedComponent?: PostFeedComponent;

  /**
   * Refreshes the post feed back to page 0 upon successful post creation.
   */
  onPostCreated(): void {
    this.feedComponent?.loadPage(0);
  }
}
