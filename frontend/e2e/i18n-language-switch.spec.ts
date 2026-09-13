import { test, expect } from '@playwright/test';

const JAPANESE_DATE_PATTERN = /^\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2}$/;
const ENGLISH_DATE_PATTERN = /^[A-Za-z]{3} \d{1,2}, \d{4}, \d{1,2}:\d{2}:\d{2} (?:AM|PM)$/;

/**
 * Automated end-to-end verification for bilingual internationalization (i18n).
 * Verifies language detection, preference persistence, UI interactions, date formats,
 * and user content authenticity without specification identifiers in assertions.
 */
test.describe('Bilingual Internationalization Workflows', () => {
  test.beforeEach(async ({ page }) => {
    const res = await page.request.get('/api/posts?page=0&size=50');
    if (res.ok()) {
      const data = await res.json();
      if (!data || data.total_items === 0 || (Array.isArray(data?.items) && data.items.length === 0)) {
        await page.request.post('/api/posts', {
          data: {
            name: 'Initial Seeder',
            title: 'Initial Seed Post',
            message: 'Seed content to ensure feed items exist for date format testing.',
          },
        });
      }
    }
  });

  test('Scenario 1: Initial access verifies language detection from browser settings and storage fallback', async ({
    page,
  }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'language', {
        get: () => 'ja-JP',
        configurable: true,
      });
      Object.defineProperty(navigator, 'languages', {
        get: () => ['ja-JP', 'ja'],
        configurable: true,
      });
    });

    await page.goto('/');
    await expect(page).toHaveURL(/\/ja\//);
    const localeBadge = page.locator('app-language-switch .locale-label');
    await expect(localeBadge).toHaveText('JA');

    const storedLocaleBefore = await page.evaluate(() => window.localStorage.getItem('bb_locale'));
    expect(storedLocaleBefore).toBeNull();

    await page.evaluate(() => {
      window.localStorage.setItem('bb_locale', 'en');
    });
    await page.goto('/');
    await expect(page).toHaveURL(/\/en\//);
    await expect(localeBadge).toHaveText('EN');
  });

  test('Scenario 2: Toolbar language toggle switches UI, updates date formatting, and persists preference', async ({
    page,
  }) => {
    await page.goto('/en/');
    await page.locator('.app-toolbar').waitFor({ state: 'visible' });

    const toggleButton = page.locator('app-language-switch button.language-switch-button');
    const localeLabel = page.locator('app-language-switch .locale-label');
    await expect(toggleButton).toBeVisible();
    await expect(toggleButton).toHaveAttribute('aria-label');
    await expect(localeLabel).toHaveText('EN');

    const timestampElement = page.locator('.post-cards-list .post-date').first();
    await expect(timestampElement).toBeVisible();
    await expect(timestampElement).toHaveText(ENGLISH_DATE_PATTERN);

    await toggleButton.click();
    await expect(page).toHaveURL(/\/ja\//);

    await expect(localeLabel).toHaveText('JA');
    await page.waitForFunction(() => window.localStorage.getItem('bb_locale') === 'ja');
    const persistedLocale = await page.evaluate(() => window.localStorage.getItem('bb_locale'));
    expect(persistedLocale).toBe('ja');

    await expect(timestampElement).toHaveText(JAPANESE_DATE_PATTERN);
  });

  test('Scenario 3: Form validation errors and submission feedback display localized messages', async ({
    page,
  }) => {
    // 1. Verify English locale form validation and submission
    await page.goto('/en/');
    await page.locator('.fixed-form-container').waitFor({ state: 'visible' });

    const nameInputEn = page.locator('input[formControlName="name"]');
    const titleInputEn = page.locator('input[formControlName="title"]');
    const messageInputEn = page.locator('textarea[formControlName="message"]');
    const submitButtonEn = page.locator('.submit-button');

    await nameInputEn.focus();
    await nameInputEn.blur();
    await submitButtonEn.click();

    const nameErrorEn = page.locator('mat-error').first();
    await expect(nameErrorEn).toBeVisible();
    await expect(nameErrorEn).toHaveText('Name must not be blank');

    const uniqueTitleEn = `Validation_Feedback_EN_${Date.now()}`;
    await nameInputEn.fill('English Author');
    await titleInputEn.fill(uniqueTitleEn);
    await messageInputEn.fill('English submission feedback test message body.');
    await submitButtonEn.click();

    const snackBarEn = page.locator('mat-snack-bar-container');
    await expect(snackBarEn).toBeVisible({ timeout: 5000 });
    await expect(snackBarEn).toContainText('Post submitted successfully!');

    // Wait for snackbar to dismiss before next test sequence
    await expect(snackBarEn).toBeHidden({ timeout: 6000 }).catch(() => {});

    // 2. Verify Japanese locale form validation and submission
    await page.goto('/ja/');
    await page.locator('.fixed-form-container').waitFor({ state: 'visible' });

    const nameInputJa = page.locator('input[formControlName="name"]');
    const titleInputJa = page.locator('input[formControlName="title"]');
    const messageInputJa = page.locator('textarea[formControlName="message"]');
    const submitButtonJa = page.locator('.submit-button');

    await nameInputJa.focus();
    await nameInputJa.blur();
    await submitButtonJa.click();

    const nameErrorJa = page.locator('mat-error').first();
    await expect(nameErrorJa).toBeVisible();
    await expect(nameErrorJa).toHaveText('名前を入力してください');

    const uniqueTitleJa = `検証用_投稿_${Date.now()}`;
    await nameInputJa.fill('日本語 投稿者');
    await titleInputJa.fill(uniqueTitleJa);
    await messageInputJa.fill('日本語のバリデーション・フィードバック検証用本文です。');
    await submitButtonJa.click();

    const snackBarJa = page.locator('mat-snack-bar-container');
    await expect(snackBarJa).toBeVisible({ timeout: 5000 });
    await expect(snackBarJa).toContainText('投稿が完了しました！');
  });

  test('Scenario 4: User-contributed posts are displayed unaltered in original language without machine translation', async ({
    page,
  }) => {
    await page.goto('/');
    await page.locator('.fixed-form-container').waitFor({ state: 'visible' });

    const originalName = '山田 太郎 🌟';
    const originalTitle = `【検証】多言語テスト投稿_${Date.now()}`;
    const originalMessage = 'これは自動翻訳されず、入力されたそのままの日本語で表示されるべき本文です。\nLine 2 with Emoji: 🚀🎉';

    await page.fill('input[formControlName="name"]', originalName);
    await page.fill('input[formControlName="title"]', originalTitle);
    await page.fill('textarea[formControlName="message"]', originalMessage);

    const submitButton = page.locator('.submit-button');
    await submitButton.click();

    const firstCard = page.locator('.post-cards-list .post-card').first();
    const firstCardTitle = firstCard.locator('.post-title');
    await expect(firstCardTitle).toHaveText(originalTitle, { timeout: 10000 });

    const renderedName = await firstCard.locator('.post-name').textContent();
    const renderedMessage = await firstCard.locator('.post-message').textContent();

    expect(renderedName?.trim()).toContain(originalName);
    expect(renderedMessage?.trim()).toBe(originalMessage);

    const toggleButton = page.locator('app-language-switch button.language-switch-button');
    await toggleButton.click();

    await expect(firstCardTitle).toHaveText(originalTitle, { timeout: 10000 });
    await expect(firstCard.locator('.post-name')).toContainText(originalName);
    await expect(firstCard.locator('.post-message')).toHaveText(originalMessage);
  });
});
