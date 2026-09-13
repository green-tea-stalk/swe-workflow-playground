import { test, expect } from '@playwright/test';

test.describe('Bulletin board feed scroll and layout verification', () => {
  test.beforeAll(async ({ request }) => {
    // Ensure sufficient posts exist for scrollability and layout verification
    const res = await request.get('/api/posts?page=0&size=50');
    if (res.ok()) {
      const data = await res.json();
      if (data.total_items < 10) {
        const postsToCreate = 10 - data.total_items;
        for (let i = 1; i <= postsToCreate; i++) {
          await request.post('/api/posts', {
            data: {
              name: `Seed User ${i}`,
              title: `Seed Post Title ${i}`,
              message: `This is automated seed message content ${i} for scroll and layout verification.\nProviding multiple lines to ensure feed overflow.\nLine 2\nLine 3`,
            },
          });
        }
      }
    }
  });

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for feed cards list to become visible
    await page.locator('.post-cards-list').waitFor({ state: 'visible' });
  });

  test('should display toolbar, message feed, paginator, and fixed bottom form', async ({ page }) => {
    // Toolbar
    await expect(page.locator('.app-toolbar')).toBeVisible();

    // Message feed
    await expect(page.locator('.post-cards-list')).toBeVisible();

    // Paginator controls
    await expect(page.locator('.paginator-wrapper')).toBeVisible();

    // Fixed bottom form
    await expect(page.locator('.fixed-form-container')).toBeVisible();
  });

  test('should allow message feed to scroll when content exceeds viewport (regression prevention)', async ({ page }) => {
    const cardsList = page.locator('.post-cards-list');
    await expect(cardsList).toBeVisible();

    // Compare scrollHeight and clientHeight to verify scrollability
    const scrollInfo = await cardsList.evaluate((el) => ({
      clientHeight: el.clientHeight,
      scrollHeight: el.scrollHeight,
      canScroll: el.scrollHeight > el.clientHeight,
      overflowY: window.getComputedStyle(el).overflowY,
    }));

    expect(scrollInfo.overflowY).toBe('auto');
    expect(scrollInfo.canScroll).toBe(true);
    expect(scrollInfo.scrollHeight).toBeGreaterThan(scrollInfo.clientHeight);

    // Verify scroll operation takes effect
    await cardsList.evaluate((el) => {
      el.scrollTop = 300;
    });

    const scrollTop = await cardsList.evaluate((el) => el.scrollTop);
    expect(scrollTop).toBeGreaterThan(0);
  });

  test('paginator must remain visible and unobstructed above fixed bottom form (regression prevention)', async ({ page }) => {
    const paginator = page.locator('.paginator-wrapper');
    const form = page.locator('.fixed-form-container');

    await expect(paginator).toBeVisible();
    await expect(form).toBeVisible();

    const paginatorBox = await paginator.boundingBox();
    const formBox = await form.boundingBox();

    expect(paginatorBox).not.toBeNull();
    expect(formBox).not.toBeNull();

    if (paginatorBox && formBox) {
      // Bottom of paginator must be less than or equal to top of fixed form container
      expect(paginatorBox.y + paginatorBox.height).toBeLessThanOrEqual(formBox.y + 1);
    }
  });

  test('submitting a new post from bottom form should prepend it to top of message feed', async ({ page }) => {
    const uniqueTitle = `E2E自動テスト_${Date.now()}`;
    const uniqueMessage = 'Playwright による E2E 自動検証投稿メッセージです。';

    await page.fill('input[formControlName="name"]', 'E2Eテスター');
    await page.fill('input[formControlName="title"]', uniqueTitle);
    await page.fill('textarea[formControlName="message"]', uniqueMessage);

    // Click submit button
    const submitButton = page.locator('.submit-button');
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    // Verify success snackbar appears
    await expect(page.locator('mat-snack-bar-container')).toBeVisible({ timeout: 5000 });

    // Verify new post appears at the top of the feed
    const firstCardTitle = page.locator('.post-cards-list .post-card .post-title').first();
    await expect(firstCardTitle).toHaveText(uniqueTitle);
  });
});
