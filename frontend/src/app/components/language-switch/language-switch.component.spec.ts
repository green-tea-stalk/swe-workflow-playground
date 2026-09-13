import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, afterEach, vi } from 'vitest';
import { By } from '@angular/platform-browser';
import { LanguageSwitchComponent } from './language-switch.component';
import { LocaleService, type SupportedLocale } from '../../services/locale.service';

describe('LanguageSwitchComponent', () => {
  let component: LanguageSwitchComponent;
  let fixture: ComponentFixture<LanguageSwitchComponent>;
  let mockLocaleService: {
    getActiveLocale: ReturnType<typeof vi.fn>;
    setLocale: ReturnType<typeof vi.fn>;
  };

  const setupComponentWithActiveLocale = async (activeLocale: SupportedLocale) => {
    mockLocaleService = {
      getActiveLocale: vi.fn().mockReturnValue(activeLocale),
      setLocale: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LanguageSwitchComponent],
      providers: [
        { provide: LocaleService, useValue: mockLocaleService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageSwitchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  };

  afterEach(() => {
    vi.restoreAllMocks();
    TestBed.resetTestingModule();
  });

  describe('initialization and rendering', () => {
    it.each([
      { activeLocale: 'en' as const, expectedLabel: 'EN' },
      { activeLocale: 'ja' as const, expectedLabel: 'JA' },
    ])('should display "$expectedLabel" when active locale is "$activeLocale"', async ({ activeLocale, expectedLabel }) => {
      await setupComponentWithActiveLocale(activeLocale);

      const labelElement = fixture.debugElement.query(By.css('.locale-label'));
      expect(labelElement).not.toBeNull();
      expect(labelElement.nativeElement.textContent.trim()).toBe(expectedLabel);
      expect(component.currentLocale()).toBe(activeLocale);
    });

    it('should configure accessible aria-label on toggle button', async () => {
      await setupComponentWithActiveLocale('en');

      const button = fixture.debugElement.query(By.css('button.language-switch-button'));
      expect(button).not.toBeNull();
      expect(button.nativeElement.getAttribute('aria-label')).toBe('Switch Language');
    });
  });

  describe('user interaction and locale switching', () => {
    it.each([
      { current: 'en' as const, expectedNext: 'ja' as const },
      { current: 'ja' as const, expectedNext: 'en' as const },
    ])('should invoke LocaleService.setLocale("$expectedNext") when clicked from "$current"', async ({ current, expectedNext }) => {
      await setupComponentWithActiveLocale(current);

      const button = fixture.debugElement.query(By.css('button.language-switch-button'));
      button.nativeElement.click();
      fixture.detectChanges();

      expect(mockLocaleService.setLocale).toHaveBeenCalledTimes(1);
      expect(mockLocaleService.setLocale).toHaveBeenCalledWith(expectedNext);
    });

    it.each([
      { target: 'en' as const },
      { target: 'ja' as const },
    ])('should delegate directly to LocaleService.setLocale("$target") when switchLocale is called', async ({ target }) => {
      await setupComponentWithActiveLocale('en');

      component.switchLocale(target);

      expect(mockLocaleService.setLocale).toHaveBeenCalledWith(target);
    });

    it.each([
      { invalidLocale: 'fr' },
      { invalidLocale: '' },
      { invalidLocale: 'EN' },
      { invalidLocale: 'ja-JP' },
    ])('should reject invalid or unsupported locale "$invalidLocale" with Error', async ({ invalidLocale }) => {
      await setupComponentWithActiveLocale('en');

      expect(() => component.switchLocale(invalidLocale as SupportedLocale)).toThrowError(
        /unsupported target locale/i
      );
      expect(mockLocaleService.setLocale).not.toHaveBeenCalled();
    });
  });
});
