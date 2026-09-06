import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { Subject, of, throwError } from 'rxjs';
import { BulletinBoardComponent } from './bulletin-board.component';
import { MessageApiService } from '../../services/message-api.service';
import { MessageCreateRequest, MessageResponse, PageResponse } from '../../models/message.model';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('BulletinBoardComponent', () => {
  let component: BulletinBoardComponent;
  let fixture: ComponentFixture<BulletinBoardComponent>;
  let mockApiService: jasmine.SpyObj<MessageApiService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const initialMockMessages: MessageResponse[] = [
    {
      id: 10,
      name: 'User 10',
      email: 'user10@example.com',
      title: 'Message 10',
      message: 'Body 10',
      createdAt: '2026-09-06T12:00:00Z',
    },
  ];

  const initialMockPagination: PageResponse<MessageResponse> = {
    content: initialMockMessages,
    page: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
  };

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj<MessageApiService>('MessageApiService', [
      'getMessages',
      'postMessage',
    ]);
    mockSnackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    mockApiService.getMessages.and.returnValue(of(initialMockPagination));

    await TestBed.configureTestingModule({
      imports: [BulletinBoardComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: MessageApiService, useValue: mockApiService },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BulletinBoardComponent);
    component = fixture.componentInstance;
  });

  it('should create bulletin board component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should automatically load first page (page 0) on initialization', () => {
    fixture.detectChanges();

    expect(mockApiService.getMessages).toHaveBeenCalledWith(0, 50);
    expect(component.messages()).toEqual(initialMockMessages);
    expect(component.pagination()).toEqual(initialMockPagination);
  });

  it('should display progress bar when request is pending', () => {
    const subject = new Subject<PageResponse<MessageResponse>>();
    mockApiService.getMessages.and.returnValue(subject.asObservable());

    component.loadMessages(0);
    fixture.detectChanges();

    expect(component.isLoading()).toBeTrue();
    const progressBar = fixture.debugElement.query(By.css('.loading-bar'));
    expect(progressBar).not.toBeNull();

    subject.next(initialMockPagination);
    subject.complete();
    fixture.detectChanges();

    expect(component.isLoading()).toBeFalse();
    expect(fixture.debugElement.query(By.css('.loading-bar'))).toBeNull();
  });

  it('should load requested page index when pageChange is triggered from message list', () => {
    fixture.detectChanges();

    const page2Messages: MessageResponse[] = [
      {
        id: 5,
        name: 'User 5',
        email: null,
        title: 'Message 5',
        message: 'Body 5',
        createdAt: '2026-09-06T10:00:00Z',
      },
    ];
    const page2Pagination: PageResponse<MessageResponse> = {
      content: page2Messages,
      page: 1,
      size: 50,
      totalElements: 60,
      totalPages: 2,
    };

    mockApiService.getMessages.and.returnValue(of(page2Pagination));

    component.loadMessages(1);

    expect(mockApiService.getMessages).toHaveBeenCalledWith(1, 50);
    expect(component.messages()).toEqual(page2Messages);
    expect(component.pagination()).toEqual(page2Pagination);
  });

  it('should display error notice and snackbar when message retrieval fails', () => {
    mockApiService.getMessages.and.returnValue(
      throwError(() => ({ error: { detail: 'データ取得に失敗しました。' } }))
    );

    component.loadMessages(0);
    fixture.detectChanges();

    expect(component.errorMessage()).toBe('データ取得に失敗しました。');
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'データ取得に失敗しました。',
      '閉じる',
      jasmine.objectContaining({ duration: 5000 })
    );

    const errorBanner = fixture.debugElement.query(By.css('.error-banner'));
    expect(errorBanner).not.toBeNull();
    expect(errorBanner.nativeElement.textContent).toContain('データ取得に失敗しました。');
  });

  it('should submit new message, reset form, reload page 0, and show toast on submission success', () => {
    fixture.detectChanges();

    const newRequest: MessageCreateRequest = {
      name: 'New Poster',
      email: null,
      title: 'New Subject',
      message: 'New message body content',
    };

    const createdResponse: MessageResponse = {
      id: 11,
      name: 'New Poster',
      email: null,
      title: 'New Subject',
      message: 'New message body content',
      createdAt: '2026-09-06T13:00:00Z',
    };

    mockApiService.postMessage.and.returnValue(of(createdResponse));
    spyOn(component, 'loadMessages').and.callThrough();

    expect(component.messageFormComponent).toBeDefined();
    const resetFormSpy = spyOn(component.messageFormComponent!, 'resetForm');

    component.handleMessageSubmitted(newRequest);

    expect(mockApiService.postMessage).toHaveBeenCalledWith(newRequest);
    expect(resetFormSpy).toHaveBeenCalled();
    expect(component.loadMessages).toHaveBeenCalledWith(0);
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'メッセージを投稿しました。',
      '閉じる',
      jasmine.objectContaining({ duration: 3000 })
    );
  });

  it('should not submit new message when request is already in-flight', () => {
    fixture.detectChanges();

    component.isLoading.set(true);

    const newRequest: MessageCreateRequest = {
      name: 'Duplicate',
      email: null,
      title: 'Duplicate',
      message: 'Duplicate',
    };

    component.handleMessageSubmitted(newRequest);

    expect(mockApiService.postMessage).not.toHaveBeenCalled();
  });

  it('should show error banner and snackbar when message submission fails with RFC 9457 problem details', () => {
    fixture.detectChanges();

    const newRequest: MessageCreateRequest = {
      name: 'Test',
      email: null,
      title: 'Test',
      message: 'Test',
    };

    const problemError = {
      error: {
        type: 'https://example.com/errors/validation',
        title: 'Bad Request',
        status: 400,
        detail: 'バリデーションエラーが発生しました。',
        instance: '/api/messages',
      },
    };

    mockApiService.postMessage.and.returnValue(throwError(() => problemError));

    component.handleMessageSubmitted(newRequest);
    fixture.detectChanges();

    expect(component.errorMessage()).toBe('バリデーションエラーが発生しました。');
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'バリデーションエラーが発生しました。',
      '閉じる',
      jasmine.objectContaining({ duration: 5000 })
    );

    const errorBanner = fixture.debugElement.query(By.css('.error-banner'));
    expect(errorBanner).not.toBeNull();
    expect(errorBanner.nativeElement.textContent).toContain('バリデーションエラーが発生しました。');
  });

  it('should maintain independent scrolling layout structure with sticky bottom form', () => {
    fixture.detectChanges();

    const scrollableArea = fixture.debugElement.query(By.css('.scrollable-feed-area'));
    expect(scrollableArea).not.toBeNull();
    expect(scrollableArea.query(By.css('app-message-list'))).not.toBeNull();
    expect(scrollableArea.query(By.css('app-message-form'))).toBeNull();

    const stickyFooter = fixture.debugElement.query(By.css('.sticky-form-footer'));
    expect(stickyFooter).not.toBeNull();
    expect(stickyFooter.query(By.css('app-message-form'))).not.toBeNull();
  });
});
