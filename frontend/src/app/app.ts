import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { PostFeedComponent } from './components/post-feed/post-feed.component';

/**
 * Root application component coordinating navigation header, message feed, and persistent bottom form.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MatToolbarModule, PostFeedComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  /** Application display title. */
  readonly title = '掲示板アプリケーション';
}
