import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { MessageApiService } from './services/message-api.service';
import { By } from '@angular/platform-browser';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let app: AppComponent;
  let mockApiService: jasmine.SpyObj<MessageApiService>;

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj<MessageApiService>('MessageApiService', [
      'getMessages',
      'postMessage',
    ]);
    mockApiService.getMessages.and.returnValue(
      of({
        content: [],
        page: 0,
        size: 50,
        totalElements: 0,
        totalPages: 0,
      })
    );

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: MessageApiService, useValue: mockApiService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    app = fixture.componentInstance;
  });

  it('should create the root app component', () => {
    expect(app).toBeTruthy();
  });

  it(`should have 'bulletin-board' as title`, () => {
    expect(app.title).toEqual('bulletin-board');
  });

  it('should render the bulletin board root component in template', () => {
    fixture.detectChanges();
    const bulletinBoardEl = fixture.debugElement.query(By.css('app-bulletin-board'));
    expect(bulletinBoardEl).not.toBeNull();
  });
});
