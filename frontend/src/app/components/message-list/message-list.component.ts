import { Component, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MessageResponse, PageResponse } from '../../models/message.model';

/**
 * Message list presentation component.
 *
 * Displays a list of messages in the exact order provided and renders pagination
 * controls. Delegates all page change actions to the parent container.
 */
@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [DatePipe, MatCardModule, MatPaginatorModule],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.scss',
})
export class MessageListComponent {
  /**
   * The list of messages to display, rendered in array sequence.
   */
  readonly messages = input<MessageResponse[]>([]);

  /**
   * Current pagination metadata, or null if uninitialized.
   */
  readonly pagination = input<PageResponse<MessageResponse> | null>(null);

  /**
   * Event emitted when the user selects a different page, providing the zero-based page index.
   */
  readonly pageChange = output<number>();

  /**
   * Handles page change events from the paginator and delegates the zero-based page index upstream.
   *
   * @param event The paginator event containing the new page index
   */
  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event.pageIndex);
  }
}

