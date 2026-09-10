import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PostFormComponent } from './post-form.component';
import { PostApiService } from '../../services/post-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PostResponse } from '../../models/post.model';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('PostFormComponent (画面下部固定投稿フォームコンポーネントテスト)', () => {
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

  describe('初期表示・入力バリデーション', () => {
    it('初期状態で全入力フィールドが空であり、フォームが無効(invalid)であること', () => {
      expect(component.postForm.value).toEqual({
        name: '',
        email: '',
        title: '',
        message: '',
      });
      expect(component.postForm.invalid).toBe(true);
    });

    it.each([
      { value: '', expectedValid: false, desc: '空文字は無効' },
      { value: '   ', expectedValid: false, desc: '空白のみは無効' },
      { value: 'a'.repeat(50), expectedValid: true, desc: '境界値(50文字)は有効' },
      { value: 'a'.repeat(51), expectedValid: false, desc: '上限超過(51文字)は無効' },
    ])('お名前 (name) バリデーション: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.name;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: true, desc: '空文字は任意のため有効' },
      { value: '   ', expectedValid: true, desc: '空白のみは任意のため有効' },
      { value: 'invalid-email-format', expectedValid: false, desc: '不正なメール形式は無効' },
      { value: 'user@example.com', expectedValid: true, desc: '正常なメール形式は有効' },
      { value: 'a'.repeat(243) + '@example.com', expectedValid: false, desc: '上限超過(255文字)は無効' },
    ])('メールアドレス (email) バリデーション: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.email;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: false, desc: '空文字は無効' },
      { value: '   ', expectedValid: false, desc: '空白のみは無効' },
      { value: 'a'.repeat(100), expectedValid: true, desc: '境界値(100文字)は有効' },
      { value: 'a'.repeat(101), expectedValid: false, desc: '上限超過(101文字)は無効' },
    ])('タイトル (title) バリデーション: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.title;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });

    it.each([
      { value: '', expectedValid: false, desc: '空文字は無効' },
      { value: '   ', expectedValid: false, desc: '空白のみは無効' },
      { value: 'a'.repeat(4000), expectedValid: true, desc: '境界値(4000文字)は有効' },
      { value: 'a'.repeat(4001), expectedValid: false, desc: '上限超過(4001文字)は無効' },
    ])('メッセージ本文 (message) バリデーション: $desc', ({ value, expectedValid }) => {
      const control = component.postForm.controls.message;
      control.setValue(value);
      expect(control.valid).toBe(expectedValid);
    });
  });

  describe('フォーム送信処理 (onSubmit)', () => {
    it('送信処理中(isSubmittingがtrue)の場合、onSubmit()が再度呼び出されても重複してAPIを呼び出さないこと', () => {
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

    it('入力不備がある場合、APIを呼び出さず全項目をtouchedにして入力を保持すること', () => {
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

    it('正常入力時、トリムされた値でAPIを呼び出し、成功時にフォーム初期化・スナックバー表示・イベント発火を行うこと', () => {
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
        expect.stringContaining('投稿しました'),
        expect.any(String),
        expect.any(Object)
      );
      expect(postCreatedEmitted).toBe(true);
      expect(component.isSubmitting()).toBe(false);
    });

    it('メールアドレスが空欄または空白の場合、APIへnullとして正規化して送信すること', () => {
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

    it('API送信失敗時、入力値を消去せず保持し、エラースナックバーを表示すること', () => {
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
        expect.stringContaining('投稿処理中にエラーが発生しました。'),
        expect.any(String),
        expect.any(Object)
      );
      expect(component.postForm.value.name).toBe('鈴木一郎');
      expect(component.postForm.value.message).toBe('失敗予定本文');
      expect(postCreatedEmitted).toBe(false);
      expect(component.isSubmitting()).toBe(false);
    });

    it('API送信失敗時(RFC 9457 Problem Details形式)、detailのエラー内容をスナックバーに表示すること', () => {
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
        '閉じる',
        expect.any(Object)
      );
      expect(component.postForm.value.name).toBe('テストユーザー');
      expect(component.isSubmitting()).toBe(false);
    });
  });

  describe('resetForm()', () => {
    it('フォーム入力をリセットし、未入力状態に戻すこと', () => {
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
