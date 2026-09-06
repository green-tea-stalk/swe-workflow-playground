import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageFormComponent } from '../message-form/message-form.component';
import { MessageApiService } from '../../services/message-api.service';
import { MessageCreateRequest, MessageResponse, PageResponse } from '../../models/message.model';

/**
 * Root bulletin board orchestrator component.
 *
 * Coordinates state between message list feed, pagination controls,
 * and bottom sticky message creation form. Handles auto-refresh on new posts.
 */
@Component({
  selector: 'app-bulletin-board',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatProgressBarModule,
    MessageListComponent,
    MessageFormComponent,
  ],
  templateUrl: './bulletin-board.component.html',
  styleUrl: './bulletin-board.component.scss',
})
export class BulletinBoardComponent implements OnInit {
  private readonly messageApiService = inject(MessageApiService);
  private readonly snackBar = inject(MatSnackBar);

  /**
   * Reference to child message form component for form resets.
   */
  @ViewChild(MessageFormComponent) messageFormComponent?: MessageFormComponent;

  /**
   * Active list of messages on the current page.
   */
  readonly messages = signal<MessageResponse[]>([]);

  /**
   * Current pagination metadata, or null if uninitialized.
   */
  readonly pagination = signal<PageResponse<MessageResponse> | null>(null);

  /**
   * Indicates whether an asynchronous HTTP request is currently pending.
   */
  readonly isLoading = signal<boolean>(false);

  /**
   * Current error message to display in the alert banner, or null if no error.
   */
  readonly errorMessage = signal<string | null>(null);

  /**
   * Page size constant for bulletin board pagination.
   */
  readonly pageSize = 50;

  /**
   * Initializes the component lifecycle by loading the initial page of messages.
   */
  ngOnInit(): void {
    this.loadMessages(0);
  }

  /**
   * Fetches messages for the specified zero-based page index.
   *
   * @param pageIndex Zero-based page index to retrieve
   */
  loadMessages(pageIndex: number): void {
    this.errorMessage.set(null);
    this.isLoading.set(true);
    this.messageApiService.getMessages(pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.messages.set(response.content);
        this.pagination.set(response);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        const detail =
          typeof err?.error?.detail === 'string'
            ? err.error.detail
            : 'メッセージの取得に失敗しました。';
        this.errorMessage.set(detail);
        this.snackBar.open(detail, '閉じる', { duration: 5000 });
      },
    });
  }

  /**
   * Handles valid message submission from child form component.
   *
   * Submits new message to backend API, resets input form, and reloads
   * first page to immediately display the newly created post at the top.
   *
   * @param newMsg New message request payload
   */
  handleMessageSubmitted(newMsg: MessageCreateRequest): void {
    if (this.isLoading()) {
      return;
    }

    this.errorMessage.set(null);
    this.isLoading.set(true);
    this.messageApiService.postMessage(newMsg).subscribe({
      next: () => {
        this.messageFormComponent?.resetForm();
        this.loadMessages(0);
        this.snackBar.open('メッセージを投稿しました。', '閉じる', { duration: 3000 });
      },
      error: (err) => {
        this.isLoading.set(false);
        const detail =
          typeof err?.error?.detail === 'string'
            ? err.error.detail
            : 'メッセージの投稿に失敗しました。';
        this.errorMessage.set(detail);
        this.snackBar.open(detail, '閉じる', { duration: 5000 });
      },
    });
  }
}
