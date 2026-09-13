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
      window.localStorage.clear();
      Object.defineProperty(navigator, 'language', {
        get: () => 'ja-JP',
        configurable: true,
      });
    });

    await page.goto('/');
    const localeBadge = page.locator('app-language-switch .locale-label');
    await expect(localeBadge).toHaveText('JA');

    const storedLocaleBefore = await page.evaluate(() => window.localStorage.getItem('bb_locale'));
    expect(storedLocaleBefore).toBeNull();

    await page.evaluate(() => {
      window.localStorage.setItem('bb_locale', 'en');
    });
    await page.goto('/en/');
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

    await expect(localeLabel).toHaveText('JA');
    await page.waitForFunction(() => window.localStorage.getItem('bb_locale') === 'ja');
    const persistedLocale = await page.evaluate(() => window.localStorage.getItem('bb_locale'));
    expect(persistedLocale).toBe('ja');

    await expect(timestampElement).toHaveText(JAPANESE_DATE_PATTERN);
  });

  test('Scenario 3: Form validation errors and submission feedback display localized messages', async ({
    page,
  }) => {
    await page.goto('/');
    await page.locator('.fixed-form-container').waitFor({ state: 'visible' });

    const nameInput = page.locator('input[formControlName="name"]');
    const titleInput = page.locator('input[formControlName="title"]');
    const messageInput = page.locator('textarea[formControlName="message"]');
    const submitButton = page.locator('.submit-button');

    await nameInput.focus();
    await nameInput.blur();
    await submitButton.click();

    const nameError = page.locator('mat-error').first();
    await expect(nameError).toBeVisible();
    await expect(nameError).toHaveText(/^(?:Name must not be blank|名前を入力してください)$/);

    const uniqueTitle = `Validation_Feedback_${Date.now()}`;
    await nameInput.fill('Feedback Author');
    await titleInput.fill(uniqueTitle);
    await messageInput.fill('Submission feedback test message body.');

    await submitButton.click();

    const snackBar = page.locator('mat-snack-bar-container');
    await expect(snackBar).toBeVisible({ timeout: 5000 });
    await expect(snackBar).toContainText(/(?:Post submitted successfully!|投稿が完了しました！)/);
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
