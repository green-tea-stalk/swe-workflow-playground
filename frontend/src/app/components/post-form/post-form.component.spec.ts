import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PostFormComponent } from './post-form.component';
import { PostApiService } from '../../services/post-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PostResponse } from '../../models/post.model';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('PostFormComponent (Fixed Bottom Post Form Unit)', () => {
  let component: PostFormComponent;
  let fixture: ComponentFixture<PostFormComponent>;
  let mockPostApiService: { createPost: ReturnType<typeof vi.fn> };
  let mockSnackBar: { open: ReturnType<typeof vi.fn> };

  const sampleCreatedPost: PostResponse = {
    id: 1,
    name: '田中太郎',
    email: 'tanaka@example.com',
    title: 'テストタイトル',
    message: 'テスト本文です。',
    created_at: '2026-09-11T12:00:00Z',
  };

  beforeEach(async () => {
    mockPostApiService = {
      createPost: vi.fn().mockReturnValue(of(sampleCreatedPost)),
    };
    mockSnackBar = {
      open: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PostFormComponent],
      providers: [
        { provide: PostApiService, useValue: mockPostApiService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initial display and input validation', () => {
    it('should have all fields empty and form marked invalid initially', () => {
      expect(component.postForm.value).toEqual({
        name: '',
        email: '',
        title: '',
        message: '',
      });
      expect(component.postForm.invalid).toBe(true);
    });

    it.each([
      { value: '', expectedValid: false, desc: 'empty string is invalid' },
      { value: '   ', expectedValid: false, desc: 'whitespace-only is invalid' },
      { value: 'a'.repeat(50), expectedValid: true, desc: 'boundary value (50 chars) is valid' },
      { value: 'a'.repeat(51), expectedValid: false, desc: 'exceeding limit (51 chars) is invalid' },
    ])('name validation: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.name;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: true, desc: 'empty string is optional and valid' },
      { value: '   ', expectedValid: true, desc: 'whitespace-only is optional and valid' },
      { value: 'invalid-email-format', expectedValid: false, desc: 'invalid email format is invalid' },
      { value: 'user@example.com', expectedValid: true, desc: 'valid email format is valid' },
      {
        value: 'a'.repeat(64) + '@' + 'b'.repeat(63) + '.' + 'c'.repeat(60) + '.' + 'd'.repeat(60) + '.com',
        expectedValid: true,
        desc: 'boundary value (254 chars) is valid',
      },
      {
        value: 'a'.repeat(64) + '@' + 'b'.repeat(64) + '.' + 'c'.repeat(60) + '.' + 'd'.repeat(60) + '.com',
        expectedValid: false,
        desc: 'exceeding limit (255 chars) is invalid',
      },
    ])('email validation: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.email;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: false, desc: 'empty string is invalid' },
      { value: '   ', expectedValid: false, desc: 'whitespace-only is invalid' },
      { value: 'a'.repeat(100), expectedValid: true, desc: 'boundary value (100 chars) is valid' },
      { value: 'a'.repeat(101), expectedValid: false, desc: 'exceeding limit (101 chars) is invalid' },
    ])('title validation: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.title;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: false, desc: 'empty string is invalid' },
      { value: '   ', expectedValid: false, desc: 'whitespace-only is invalid' },
      { value: 'a'.repeat(4000), expectedValid: true, desc: 'boundary value (4000 chars) is valid' },
      { value: 'a'.repeat(4001), expectedValid: false, desc: 'exceeding limit (4001 chars) is invalid' },
    ])('message validation: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.message;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });
  });

  describe('Form submission (onSubmit)', () => {
    it('should not duplicate API call if onSubmit() is called while isSubmitting is true', () => {
      component.isSubmitting.set(true);
      component.postForm.setValue({
        name: '重複送信テスト',
        email: null,
        title: '重複タイトル',
        message: '重複本文',
      });

      component.onSubmit();

      expect(mockPostApiService.createPost).not.toHaveBeenCalled();
    });

    it('should mark all fields touched and retain inputs without calling API when form is invalid', () => {
      component.postForm.patchValue({
        name: '入力済み名前',
        email: 'invalid-email-format',
        title: '',
        message: '入力済みメッセージ',
      });

      component.onSubmit();

      expect(mockPostApiService.createPost).not.toHaveBeenCalled();
      expect(component.postForm.touched).toBe(true);
      expect(component.postForm.controls.name.value).toBe('入力済み名前');
      expect(component.postForm.controls.message.value).toBe('入力済みメッセージ');
    });

    it('should call API with trimmed values, reset form, show snackbar, and emit event upon success', () => {
      let postCreatedEmitted = false;
      component.postCreated.subscribe(() => {
        postCreatedEmitted = true;
      });

      component.postForm.setValue({
        name: '  田中太郎  ',
        email: '  tanaka@example.com  ',
        title: '  テスト件名  ',
        message: '  テスト本文です。  ',
      });

      component.onSubmit();

      expect(mockPostApiService.createPost).toHaveBeenCalledWith({
        name: '田中太郎',
        email: 'tanaka@example.com',
        title: 'テスト件名',
        message: 'テスト本文です。',
      });

      expect(component.postForm.value).toEqual({
        name: null,
        email: null,
        title: null,
        message: null,
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Post submitted successfully!',
        'Close',
        expect.any(Object)
      );
      expect(postCreatedEmitted).toBe(true);
      expect(component.isSubmitting()).toBe(false);
    });

    it('should normalize blank or whitespace email to null when sending to API', () => {
      component.postForm.setValue({
        name: '佐藤花子',
        email: '   ',
        title: 'メールなしタイトル',
        message: 'メールなし本文',
      });

      component.onSubmit();

      expect(mockPostApiService.createPost).toHaveBeenCalledWith({
        name: '佐藤花子',
        email: null,
        title: 'メールなしタイトル',
        message: 'メールなし本文',
      });
    });

    it('should retain form values and display error snackbar upon API submission failure', () => {
      mockPostApiService.createPost.mockReturnValue(
        throwError(() => new Error('サーバー内部エラーが発生しました。'))
      );

      let postCreatedEmitted = false;
      component.postCreated.subscribe(() => {
        postCreatedEmitted = true;
      });

      component.postForm.setValue({
        name: '鈴木一郎',
        email: 'ichiro@example.com',
        title: '失敗予定タイトル',
        message: '失敗予定本文',
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'An error occurred while submitting the post. Please try again later.',
        'Close',
        expect.any(Object)
      );
      expect(component.postForm.value.name).toBe('鈴木一郎');
      expect(component.postForm.value.message).toBe('失敗予定本文');
      expect(postCreatedEmitted).toBe(false);
      expect(component.isSubmitting()).toBe(false);
    });

    it('should fall back to generic error message when problem details detail is blank', () => {
      const problemErrorWithBlankDetail = {
        error: {
          type: 'https://example.com/errors/server-error',
          title: 'Error',
          status: 500,
          detail: '   ',
          instance: '/api/posts',
        },
      };
      mockPostApiService.createPost.mockReturnValue(throwError(() => problemErrorWithBlankDetail));

      component.postForm.setValue({
        name: '鈴木一郎',
        email: 'ichiro@example.com',
        title: 'テストタイトル',
        message: 'テスト本文',
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'An error occurred while submitting the post. Please try again later.',
        'Close',
        expect.any(Object)
      );
      expect(component.isSubmitting()).toBe(false);
    });

    it('should display problem details error detail in snackbar upon RFC 9457 error', () => {
      const problemError = {
        error: {
          type: 'https://example.com/errors/validation-failed',
          title: 'Validation Failed',
          status: 400,
          detail: '入力値に不正な文字が含まれています。',
          instance: '/api/posts',
        },
      };
      mockPostApiService.createPost.mockReturnValue(throwError(() => problemError));

      component.postForm.setValue({
        name: 'テストユーザー',
        email: null,
        title: 'テストタイトル',
        message: 'テスト本文',
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        '入力値に不正な文字が含まれています。',
        'Close',
        expect.any(Object)
      );
      expect(component.postForm.value.name).toBe('テストユーザー');
      expect(component.isSubmitting()).toBe(false);
    });

    it('should display English problem details error detail in snackbar upon English RFC 9457 error', () => {
      const problemError = {
        error: {
          type: 'https://example.com/errors/validation-failed',
          title: 'Validation Failed',
          status: 400,
          detail: 'Input payload failed validation constraints.',
          instance: '/api/posts',
        },
      };
      mockPostApiService.createPost.mockReturnValue(throwError(() => problemError));

      component.postForm.setValue({
        name: 'Alice',
        email: null,
        title: 'Title',
        message: 'Message',
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Input payload failed validation constraints.',
        'Close',
        expect.any(Object)
      );
      expect(component.postForm.value.name).toBe('Alice');
      expect(component.isSubmitting()).toBe(false);
    });
  });

  describe('DOM validation error rendering and localized labels', () => {
    it('should render canonical field labels in the DOM', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.form-field-name mat-label')?.textContent).toContain('Name');
      expect(compiled.querySelector('.form-field-email mat-label')?.textContent).toContain('Email');
      expect(compiled.querySelector('.form-field-title mat-label')?.textContent).toContain('Title');
      expect(compiled.querySelector('.form-field-message mat-label')?.textContent).toContain('Message');
    });

    it('should render localized error messages when controls are touched and invalid', () => {
      component.postForm.controls.name.markAsTouched();
      component.postForm.controls.title.markAsTouched();
      component.postForm.controls.message.markAsTouched();
      component.postForm.controls.email.setValue('invalid-email');
      component.postForm.controls.email.markAsTouched();
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.form-field-name mat-error')?.textContent).toContain('Name must not be blank');
      expect(compiled.querySelector('.form-field-email mat-error')?.textContent).toContain('Email must be a well-formed email address');
      expect(compiled.querySelector('.form-field-title mat-error')?.textContent).toContain('Title must not be blank');
      expect(compiled.querySelector('.form-field-message mat-error')?.textContent).toContain('Message must not be blank');
    });
  });

  describe('resetForm()', () => {
    it('should reset form inputs back to pristine unpopulated state', () => {
      component.postForm.setValue({
        name: 'リセット前名前',
        email: 'reset@example.com',
        title: 'リセット前タイトル',
        message: 'リセット前本文',
      });

      component.resetForm();

      expect(component.postForm.controls.name.value).toBeNull();
      expect(component.postForm.controls.title.value).toBeNull();
      expect(component.postForm.pristine).toBe(true);
    });
  });
});
