import { TestBed } from '@angular/core/testing';
import { DOCUMENT } from '@angular/common';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { LocaleService, type SupportedLocale } from './locale.service';

describe('LocaleService', () => {
  let service: LocaleService;
  let mockDocument: {
    location?: { href: string; pathname: string };
    baseURI: string;
  };
  let mockStorage: Record<string, string>;

  beforeEach(() => {
    mockStorage = {};

    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key: string) => {
      return mockStorage[key] ?? null;
    });
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation((key: string, value: string) => {
      mockStorage[key] = value;
    });

    mockDocument = {
      location: { href: 'http://localhost:4200/en/', pathname: '/en/' },
      baseURI: 'http://localhost:4200/en/',
    };

    TestBed.configureTestingModule({
      providers: [
        LocaleService,
        { provide: DOCUMENT, useValue: mockDocument },
      ],
    });

    service = TestBed.inject(LocaleService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('getActiveLocale', () => {
    it.each([
      { pathname: '/ja/', baseURI: 'http://localhost:4200/ja/', expected: 'ja', description: 'root /ja/ path' },
      { pathname: '/ja', baseURI: 'http://localhost:4200/ja', expected: 'ja', description: 'exact /ja path' },
      { pathname: '/en/', baseURI: 'http://localhost:4200/en/', expected: 'en', description: 'root /en/ path' },
      { pathname: '/', baseURI: 'http://localhost:4200/', expected: 'en', description: 'default root / path' },
      { pathname: '/articles/ja/', baseURI: 'http://localhost:4200/articles/ja/', expected: 'en', description: 'nested path containing ja segment' },
    ])('should resolve "$expected" for $description', ({ pathname, baseURI, expected }) => {
      if (mockDocument.location) {
        mockDocument.location.pathname = pathname;
      }
      mockDocument.baseURI = baseURI;
      expect(service.getActiveLocale()).toBe(expected);
    });

    it('should fallback to en safely when document.location is undefined', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LocaleService,
          { provide: DOCUMENT, useValue: { baseURI: '' } },
        ],
      });
      const noLocService = TestBed.inject(LocaleService);
      expect(noLocService.getActiveLocale()).toBe('en');
    });
  });

  describe('getStoredLocale', () => {
    it.each([
      { stored: 'ja', expected: 'ja', description: 'valid stored locale ja' },
      { stored: 'en', expected: 'en', description: 'valid stored locale en' },
      { stored: null, expected: null, description: 'missing key in localStorage' },
      { stored: 'fr', expected: null, description: 'unsupported locale code' },
      { stored: 'JA', expected: null, description: 'uppercase locale code' },
      { stored: 'ja-JP', expected: null, description: 'locale tag with country code' },
      { stored: '', expected: null, description: 'empty string' },
    ])('should return $expected when $description', ({ stored, expected }) => {
      if (stored !== null) {
        mockStorage['bb_locale'] = stored;
      }
      expect(service.getStoredLocale()).toBe(expected);
    });

    it('should return null safely when localStorage.getItem throws SecurityError', () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('SecurityError: Access is denied');
      });
      expect(service.getStoredLocale()).toBeNull();
    });
  });

  describe('resolveInitialLocale', () => {
    it.each([
      { stored: 'ja', browserLang: 'en-US', expected: 'ja', description: 'prioritizes valid stored locale ja over browser language' },
      { stored: 'en', browserLang: 'ja-JP', expected: 'en', description: 'prioritizes valid stored locale en over browser language' },
      { stored: null, browserLang: 'ja', expected: 'ja', description: 'resolves ja when browser language is ja' },
      { stored: null, browserLang: 'ja-JP', expected: 'ja', description: 'resolves ja when browser language is ja-JP' },
      { stored: null, browserLang: 'JA', expected: 'ja', description: 'resolves ja case-insensitively' },
      { stored: null, browserLang: 'en-US', expected: 'en', description: 'resolves en when browser language is en-US' },
      { stored: null, browserLang: 'de-DE', expected: 'en', description: 'falls back to en when browser language is unsupported' },
      { stored: null, browserLang: '', expected: 'en', description: 'falls back to en when browser language is empty' },
    ])('should resolve "$expected" when $description', ({ stored, browserLang, expected }) => {
      if (stored !== null) {
        mockStorage['bb_locale'] = stored;
      }
      vi.spyOn(navigator, 'language', 'get').mockReturnValue(browserLang);
      expect(service.resolveInitialLocale()).toBe(expected);
    });
  });

  describe('setLocale', () => {
    it.each([
      { input: 'fr', description: 'unsupported 2-letter locale' },
      { input: 'EN', description: 'uppercase locale code violating ^[a-z]{2}$' },
      { input: 'ja-JP', description: 'locale tag exceeding 2-letter format' },
      { input: 'invalid', description: 'alphabetic string exceeding 2 letters' },
      { input: '', description: 'empty string' },
      { input: 123 as unknown as string, description: 'numeric value' },
      { input: null as unknown as string, description: 'null value' },
      { input: undefined as unknown as string, description: 'undefined value' },
    ])('should reject invalid or unsupported locale "$input" ($description)', ({ input }) => {
      expect(() => service.setLocale(input as SupportedLocale)).toThrowError(/unsupported locale/i);
    });

    it('should persist valid locale into localStorage and navigate when target differs from active locale', () => {
      if (mockDocument.location) {
        mockDocument.location.pathname = '/en/';
        mockDocument.location.href = 'http://localhost:4200/en/';
      }
      mockDocument.baseURI = 'http://localhost:4200/en/';

      service.setLocale('ja');

      expect(mockStorage['bb_locale']).toBe('ja');
      expect(mockDocument.location?.href).toBe('/ja/');
    });

    it('should persist locale into localStorage but avoid redundant navigation when target matches active locale', () => {
      if (mockDocument.location) {
        mockDocument.location.pathname = '/ja/';
        mockDocument.location.href = 'http://localhost:4200/ja/';
      }
      mockDocument.baseURI = 'http://localhost:4200/ja/';
      const initialHref = mockDocument.location?.href;

      service.setLocale('ja');

      expect(mockStorage['bb_locale']).toBe('ja');
      expect(mockDocument.location?.href).toBe(initialHref);
    });

    it('should handle localStorage write errors safely without halting navigation', () => {
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('QuotaExceededError');
      });
      if (mockDocument.location) {
        mockDocument.location.pathname = '/en/';
        mockDocument.location.href = 'http://localhost:4200/en/';
      }

      service.setLocale('ja');

      expect(mockDocument.location?.href).toBe('/ja/');
    });

    it('should not throw error when document.location is undefined during navigation', () => {
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          LocaleService,
          { provide: DOCUMENT, useValue: { baseURI: 'http://localhost:4200/en/' } },
        ],
      });
      const noLocService = TestBed.inject(LocaleService);

      expect(() => noLocService.setLocale('ja')).not.toThrow();
      expect(mockStorage['bb_locale']).toBe('ja');
    });
  });
});

