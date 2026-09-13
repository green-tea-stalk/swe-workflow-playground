import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PostApiService } from '../../services/post-api.service';
import { PostResponse } from '../../models/post.model';

/**
 * Component displaying the reverse-chronological bulletin board feed,
 * empty state placeholder, and 50-item pagination controls.
 */
@Component({
  selector: 'app-post-feed',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    MatCardModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './post-feed.component.html',
  styleUrl: './post-feed.component.scss',
})
export class PostFeedComponent implements OnInit {
  private readonly postApiService = inject(PostApiService);

  /** Current list of displayed posts on the active page. */
  readonly posts = signal<readonly PostResponse[]>([]);

  /** Total count of recorded posts across all pages. */
  readonly totalItems = signal<number>(0);

  /** Zero-based current page index. */
  readonly pageIndex = signal<number>(0);

  /** Page size fixed to 50 items per specification. */
  readonly pageSize = 50;

  /** Indicates whether an API network request is in-flight. */
  readonly isLoading = signal<boolean>(false);

  /** Error message to display if the feed fails to load. */
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadPage(0);
  }

  /**
   * Fetches the specified page of posts from the backend API.
   *
   * @param page zero-based page index to load
   */
  loadPage(page: number): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.postApiService.getPosts(page, this.pageSize).subscribe({
      next: (response) => {
        this.posts.set(response.items ?? []);
        this.totalItems.set(response.total_items);
        this.pageIndex.set(response.page);
        this.isLoading.set(false);
      },
      error: (err: unknown) => {
        const errorDetail = err instanceof Error ? err.message : '投稿一覧の取得に失敗しました。';
        this.errorMessage.set(errorDetail);
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Handles user interaction with the pagination control.
   *
   * @param event the page change event emitted by MatPaginator
   */
  onPageChange(event: { pageIndex: number; pageSize?: number; length?: number }): void {
    this.loadPage(event.pageIndex);
  }

  /**
   * Refreshes the feed using the currently selected page index.
   */
  refresh(): void {
    this.loadPage(this.pageIndex());
  }
}
