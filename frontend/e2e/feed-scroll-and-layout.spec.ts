import { test, expect } from '@playwright/test';

test.describe('掲示板フィードのスクロールおよびレイアウト検証', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // フィードのロード完了を待機
    await page.locator('.post-cards-list').waitFor({ state: 'visible' });
  });

  test('ツールバー、メッセージ一覧、ページネーション、下部固定フォームがすべて表示されること', async ({ page }) => {
    // ツールバー
    await expect(page.locator('.app-toolbar')).toBeVisible();

    // メッセージ一覧
    await expect(page.locator('.post-cards-list')).toBeVisible();

    // ページネーションコントロール
    await expect(page.locator('.paginator-wrapper')).toBeVisible();

    // 画面下部固定フォーム
    await expect(page.locator('.fixed-form-container')).toBeVisible();
  });

  test('投稿数が多数の場合にメッセージ一覧欄がスクロール可能であること (再発防止)', async ({ page }) => {
    const cardsList = page.locator('.post-cards-list');
    await expect(cardsList).toBeVisible();

    // scrollHeight と clientHeight を比較してスクロール可能であることを確認
    const scrollInfo = await cardsList.evaluate((el) => ({
      clientHeight: el.clientHeight,
      scrollHeight: el.scrollHeight,
      canScroll: el.scrollHeight > el.clientHeight,
      overflowY: window.getComputedStyle(el).overflowY,
    }));

    expect(scrollInfo.overflowY).toBe('auto');
    expect(scrollInfo.canScroll).toBe(true);
    expect(scrollInfo.scrollHeight).toBeGreaterThan(scrollInfo.clientHeight);

    // スクロール操作が反映されることを確認
    await cardsList.evaluate((el) => {
      el.scrollTop = 300;
    });

    const scrollTop = await cardsList.evaluate((el) => el.scrollTop);
    expect(scrollTop).toBeGreaterThan(0);
  });

  test('ページネーションが下部固定フォームに隠れず常に画面内に可視であること (再発防止)', async ({ page }) => {
    const paginator = page.locator('.paginator-wrapper');
    const form = page.locator('.fixed-form-container');

    await expect(paginator).toBeVisible();
    await expect(form).toBeVisible();

    const paginatorBox = await paginator.boundingBox();
    const formBox = await form.boundingBox();

    expect(paginatorBox).not.toBeNull();
    expect(formBox).not.toBeNull();

    if (paginatorBox && formBox) {
      // ページネーションの下端が固定フォームの上端以下（または重なっていない）こと
      expect(paginatorBox.y + paginatorBox.height).toBeLessThanOrEqual(formBox.y + 1);
    }
  });

  test('画面下部フォームから新規投稿を行い、一覧先頭に反映されること', async ({ page }) => {
    const uniqueTitle = `E2E自動テスト_${Date.now()}`;
    const uniqueMessage = 'Playwright による E2E 自動検証投稿メッセージです。';

    await page.fill('input[formControlName="name"]', 'E2Eテスター');
    await page.fill('input[formControlName="title"]', uniqueTitle);
    await page.fill('textarea[formControlName="message"]', uniqueMessage);

    // 投稿ボタン押下
    const submitButton = page.locator('.submit-button');
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    // 成功スナックバーの表示確認
    await expect(page.locator('mat-snack-bar-container')).toBeVisible({ timeout: 5000 });

    // 一覧の最上部に新規投稿が表示されること
    const firstCardTitle = page.locator('.post-cards-list .post-card .post-title').first();
    await expect(firstCardTitle).toHaveText(uniqueTitle);
  });
});
