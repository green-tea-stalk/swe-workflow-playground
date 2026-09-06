import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { By } from '@angular/platform-browser';
import { MessageListComponent } from './message-list.component';
import { MessageResponse, PageResponse } from '../../models/message.model';

describe('MessageListComponent', () => {
  let component: MessageListComponent;
  let componentRef: ComponentRef<MessageListComponent>;
  let fixture: ComponentFixture<MessageListComponent>;

  const mockMessages: MessageResponse[] = [
    {
      id: 2,
      name: 'Alice',
      email: 'alice@example.com',
      title: 'Second Message',
      message: 'This is the newer message body.',
      createdAt: '2026-09-06T12:00:00Z',
    },
    {
      id: 1,
      name: 'Bob',
      email: null,
      title: 'First Message',
      message: 'This is the older message body.',
      createdAt: '2026-09-06T11:00:00Z',
    },
  ];

  const mockPagination: PageResponse<MessageResponse> = {
    content: mockMessages,
    page: 0,
    size: 50,
    totalElements: 2,
    totalPages: 1,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessageListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MessageListComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create the message list component', () => {
    expect(component).toBeTruthy();
  });

  it('should render messages in the exact sequence provided by the input signal', () => {
    componentRef.setInput('messages', mockMessages);
    componentRef.setInput('pagination', mockPagination);
    fixture.detectChanges();

    const cardTitles = fixture.debugElement
      .queryAll(By.css('.message-title'))
      .map((el) => el.nativeElement.textContent.trim());

    expect(cardTitles).toEqual(['Second Message', 'First Message']);

    const cardBodies = fixture.debugElement
      .queryAll(By.css('.message-body'))
      .map((el) => el.nativeElement.textContent.trim());

    expect(cardBodies).toEqual([
      'This is the newer message body.',
      'This is the older message body.',
    ]);
  });

  it('should render mailto link when email is provided and omit when null or whitespace', () => {
    const messagesWithVariedEmails: MessageResponse[] = [
      ...mockMessages,
      {
        id: 3,
        name: 'Charlie',
        email: '   ',
        title: 'Third Message',
        message: 'Message with whitespace email.',
        createdAt: '2026-09-06T10:00:00Z',
      },
    ];

    componentRef.setInput('messages', messagesWithVariedEmails);
    componentRef.setInput('pagination', {
      content: messagesWithVariedEmails,
      page: 0,
      size: 50,
      totalElements: 3,
      totalPages: 1,
    });
    fixture.detectChanges();

    const cards = fixture.debugElement.queryAll(By.css('.message-card'));
    expect(cards.length).toBe(3);

    const aliceEmailLink = cards[0].query(By.css('.author-email'));
    expect(aliceEmailLink).not.toBeNull();
    expect(aliceEmailLink.nativeElement.getAttribute('href')).toBe('mailto:alice@example.com');
    expect(aliceEmailLink.nativeElement.textContent).toContain('alice@example.com');

    const bobEmailLink = cards[1].query(By.css('.author-email'));
    expect(bobEmailLink).toBeNull();

    const charlieEmailLink = cards[2].query(By.css('.author-email'));
    expect(charlieEmailLink).toBeNull();
  });

  it('should display empty message notification when message list is empty', () => {
    componentRef.setInput('messages', []);
    componentRef.setInput('pagination', {
      content: [],
      page: 0,
      size: 50,
      totalElements: 0,
      totalPages: 0,
    });
    fixture.detectChanges();

    const cards = fixture.debugElement.queryAll(By.css('.message-card'));
    expect(cards.length).toBe(0);

    const emptyNotice = fixture.debugElement.query(By.css('.empty-feed-notice'));
    expect(emptyNotice).not.toBeNull();
    expect(emptyNotice.nativeElement.textContent.trim()).toBe('投稿されたメッセージはありません。');
  });

  it('should correctly bind pagination state to mat-paginator', () => {
    const multiPagePagination: PageResponse<MessageResponse> = {
      content: mockMessages,
      page: 1,
      size: 50,
      totalElements: 120,
      totalPages: 3,
    };

    componentRef.setInput('messages', mockMessages);
    componentRef.setInput('pagination', multiPagePagination);
    fixture.detectChanges();

    const paginatorEl = fixture.debugElement.query(By.css('mat-paginator'));
    expect(paginatorEl).not.toBeNull();

    const paginatorComponent = paginatorEl.componentInstance;
    expect(paginatorComponent.length).toBe(120);
    expect(paginatorComponent.pageSize).toBe(50);
    expect(paginatorComponent.pageIndex).toBe(1);
  });

  it('should not render paginator when pagination metadata is null', () => {
    componentRef.setInput('messages', mockMessages);
    componentRef.setInput('pagination', null);
    fixture.detectChanges();

    const paginatorEl = fixture.debugElement.query(By.css('mat-paginator'));
    expect(paginatorEl).toBeNull();
  });

  it('should emit pageChange event with 0-based page index when paginator fires page event', () => {
    componentRef.setInput('messages', mockMessages);
    componentRef.setInput('pagination', mockPagination);
    fixture.detectChanges();

    let emittedPageIndex: number | undefined;
    component.pageChange.subscribe((pageIndex: number) => {
      emittedPageIndex = pageIndex;
    });

    const paginatorEl = fixture.debugElement.query(By.css('mat-paginator'));
    expect(paginatorEl).not.toBeNull();

    const pageEvent: PageEvent = {
      pageIndex: 2,
      pageSize: 50,
      length: 120,
    };

    paginatorEl.triggerEventHandler('page', pageEvent);

    expect(emittedPageIndex).toBe(2);
  });

  it('should safely render raw HTML characters as text without executing scripts', () => {
    const maliciousMessages: MessageResponse[] = [
      {
        id: 99,
        name: '<script>alert("name-xss")</script>',
        email: '<script>alert("email-xss")</script>',
        title: '<img src=x onerror=alert(1)>',
        message: '<b>bold text payload</b>',
        createdAt: '2026-09-06T12:00:00Z',
      },
    ];

    componentRef.setInput('messages', maliciousMessages);
    componentRef.setInput('pagination', {
      content: maliciousMessages,
      page: 0,
      size: 50,
      totalElements: 1,
      totalPages: 1,
    });
    fixture.detectChanges();

    const card = fixture.debugElement.query(By.css('.message-card'));
    const titleEl = card.query(By.css('.message-title'));
    const bodyEl = card.query(By.css('.message-body'));
    const authorEl = card.query(By.css('.author-name'));
    const emailEl = card.query(By.css('.author-email'));

    expect(titleEl.nativeElement.innerHTML).not.toContain('<img');
    expect(titleEl.nativeElement.textContent).toContain('<img src=x onerror=alert(1)>');

    expect(bodyEl.nativeElement.innerHTML).not.toContain('<b>');
    expect(bodyEl.nativeElement.textContent).toContain('<b>bold text payload</b>');

    expect(authorEl.nativeElement.innerHTML).not.toContain('<script>');
    expect(authorEl.nativeElement.textContent).toContain('<script>alert("name-xss")</script>');

    expect(emailEl).not.toBeNull();
    expect(emailEl.nativeElement.innerHTML).not.toContain('<script>');
    expect(emailEl.nativeElement.textContent).toContain('<script>alert("email-xss")</script>');
  });
});
