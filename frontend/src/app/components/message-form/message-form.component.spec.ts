import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MessageFormComponent } from './message-form.component';
import { MessageCreateRequest } from '../../models/message.model';

describe('MessageFormComponent', () => {
  let component: MessageFormComponent;
  let fixture: ComponentFixture<MessageFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessageFormComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(MessageFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the message form component', () => {
    expect(component).toBeTruthy();
  });

  it('should enforce fixed viewport positioning invariant in host styles', () => {
    const hostElement = fixture.nativeElement as HTMLElement;
    const styles = window.getComputedStyle(hostElement);
    expect(styles.position).toBe('sticky');
    expect(styles.bottom).toBe('0px');
    expect(styles.zIndex).toBe('100');
  });

  it('should initialize with an invalid empty form with required error states', () => {
    expect(component.form.valid).toBeFalse();
    expect(component.form.get('name')?.hasError('required')).toBeTrue();
    expect(component.form.get('title')?.hasError('required')).toBeTrue();
    expect(component.form.get('message')?.hasError('required')).toBeTrue();
    expect(component.form.get('email')?.valid).toBeTrue();
  });

  it('should invalidate whitespace-only inputs for required fields', () => {
    component.form.setValue({
      name: '   ',
      email: '',
      title: '   ',
      message: '   ',
    });

    expect(component.form.valid).toBeFalse();
    expect(component.form.get('name')?.hasError('whitespace')).toBeTrue();
    expect(component.form.get('title')?.hasError('whitespace')).toBeTrue();
    expect(component.form.get('message')?.hasError('whitespace')).toBeTrue();
  });

  it('should validate email format only when email is provided', () => {
    const emailControl = component.form.get('email');

    emailControl?.setValue('');
    expect(emailControl?.valid).toBeTrue();

    emailControl?.setValue('invalid-email');
    expect(emailControl?.valid).toBeFalse();
    expect(emailControl?.hasError('email')).toBeTrue();

    emailControl?.setValue('user@example.com');
    expect(emailControl?.valid).toBeTrue();
  });

  it('should enforce maximum length constraints at exact valid and invalid boundaries', () => {
    const testCases = [
      {
        control: component.form.get('name')!,
        max: 50,
        validVal: 'a'.repeat(50),
        invalidVal: 'a'.repeat(51),
      },
      {
        control: component.form.get('title')!,
        max: 100,
        validVal: 'a'.repeat(100),
        invalidVal: 'a'.repeat(101),
      },
      {
        control: component.form.get('message')!,
        max: 1000,
        validVal: 'a'.repeat(1000),
        invalidVal: 'a'.repeat(1001),
      },
      {
        control: component.form.get('email')!,
        max: 100,
        validVal: `${'a'.repeat(64)}@${'b'.repeat(31)}.com`, // 64 + 1 + 31 + 4 = 100
        invalidVal: `${'a'.repeat(64)}@${'b'.repeat(32)}.com`, // 64 + 1 + 32 + 4 = 101
      },
    ];

    for (const tc of testCases) {
      tc.control.setValue(tc.validVal);
      expect(tc.control.hasError('maxlength')).toBeFalse();

      tc.control.setValue(tc.invalidVal);
      expect(tc.control.hasError('maxlength')).toBeTrue();
    }
  });

  it('should not emit submitMessage event and mark controls as touched when form is invalid', () => {
    let emitted = false;
    component.submitMessage.subscribe(() => {
      emitted = true;
    });

    component.form.setValue({
      name: '',
      email: '',
      title: '',
      message: '',
    });

    expect(component.form.touched).toBeFalse();

    component.onSubmit();

    expect(emitted).toBeFalse();
    expect(component.form.touched).toBeTrue();
    expect(component.form.get('name')?.touched).toBeTrue();
    expect(component.form.get('title')?.touched).toBeTrue();
    expect(component.form.get('message')?.touched).toBeTrue();
  });

  it('should trim string fields and normalize empty email to null on submission', () => {
    let emittedPayload: MessageCreateRequest | undefined;
    component.submitMessage.subscribe((payload: MessageCreateRequest) => {
      emittedPayload = payload;
    });

    component.form.setValue({
      name: '  Taro Yamada  ',
      email: '   ',
      title: '  Test Subject  ',
      message: '  This is a content body.  ',
    });

    expect(component.form.valid).toBeTrue();
    component.onSubmit();

    expect(emittedPayload).toBeDefined();
    expect(emittedPayload).toEqual({
      name: 'Taro Yamada',
      email: null,
      title: 'Test Subject',
      message: 'This is a content body.',
    });
  });

  it('should retain trimmed email when valid email is provided', () => {
    let emittedPayload: MessageCreateRequest | undefined;
    component.submitMessage.subscribe((payload: MessageCreateRequest) => {
      emittedPayload = payload;
    });

    component.form.setValue({
      name: 'Hanako',
      email: '  hanako@example.com  ',
      title: 'Hello',
      message: 'Greeting message',
    });

    expect(component.form.valid).toBeTrue();
    component.onSubmit();

    expect(emittedPayload).toEqual({
      name: 'Hanako',
      email: 'hanako@example.com',
      title: 'Hello',
      message: 'Greeting message',
    });
  });

  it('should trigger onSubmit through form ngSubmit DOM event', () => {
    let emittedPayload: MessageCreateRequest | undefined;
    component.submitMessage.subscribe((payload: MessageCreateRequest) => {
      emittedPayload = payload;
    });

    component.form.setValue({
      name: 'DOM Submitter',
      email: null,
      title: 'DOM Subject',
      message: 'DOM Message Body',
    });
    fixture.detectChanges();

    const formEl = fixture.debugElement.query(By.css('form'));
    formEl.triggerEventHandler('ngSubmit', null);

    expect(emittedPayload).toEqual({
      name: 'DOM Submitter',
      email: null,
      title: 'DOM Subject',
      message: 'DOM Message Body',
    });
  });

  it('should reset form state and error markers when resetForm is called', () => {
    component.form.setValue({
      name: 'Hanako',
      email: 'hanako@example.com',
      title: 'Hello',
      message: 'Greeting message',
    });

    expect(component.form.value.name).toBe('Hanako');

    component.resetForm();

    expect(component.form.value.name).toBeNull();
    expect(component.form.value.email).toBeNull();
    expect(component.form.value.title).toBeNull();
    expect(component.form.value.message).toBeNull();
    expect(component.form.pristine).toBeTrue();
    expect(component.form.untouched).toBeTrue();
  });

  it('should disable submit button in DOM when form is invalid', () => {
    component.form.patchValue({ name: '' });
    fixture.detectChanges();

    const submitBtn = fixture.debugElement.query(By.css('button[type="submit"]'));
    expect(submitBtn.nativeElement.disabled).toBeTrue();

    component.form.setValue({
      name: 'Valid Name',
      email: null,
      title: 'Valid Title',
      message: 'Valid Message',
    });
    fixture.detectChanges();

    expect(submitBtn.nativeElement.disabled).toBeFalse();
  });
});
