import { Injectable, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

/**
 * Supported application locale identifiers.
 */
export type SupportedLocale = 'en' | 'ja';

/**
 * Service managing client-side application locale state, persistence, initial resolution, and navigation.
 * Conforms to the application locale management contract.
 */
@Injectable({
  providedIn: 'root',
})
export class LocaleService {
  private static readonly STORAGE_KEY = 'bb_locale';
  private static readonly SUPPORTED_LOCALES: readonly SupportedLocale[] = ['en', 'ja'] as const;
  private static readonly DEFAULT_LOCALE: SupportedLocale = 'en';
  private static readonly LOCALE_CODE_REGEX = /^[a-z]{2}$/;

  private readonly document = inject(DOCUMENT);

  /**
   * Resolves the active application locale from the current document base Href or URL pathname root boundary.
   *
   * @returns active locale ('en' or 'ja')
   */
  getActiveLocale(): SupportedLocale {
    const pathname = this.document.location?.pathname ?? '';
    const baseURI = this.document.baseURI ?? '';

    let baseUriPath = '';
    if (baseURI) {
      try {
        baseUriPath = new URL(baseURI, 'http://localhost').pathname;
      } catch {
        baseUriPath = baseURI;
      }
    }

    if (/^\/ja(\/|$)/.test(pathname) || /^\/ja(\/|$)/.test(baseUriPath)) {
      return 'ja';
    }
    return LocaleService.DEFAULT_LOCALE;
  }

  /**
   * Reads and validates the user-selected locale preference from localStorage.
   *
   * @returns stored locale ('en' or 'ja') if valid, otherwise null
   */
  getStoredLocale(): SupportedLocale | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    try {
      const stored = localStorage.getItem(LocaleService.STORAGE_KEY);
      if (stored !== null && (LocaleService.SUPPORTED_LOCALES as readonly string[]).includes(stored)) {
        return stored as SupportedLocale;
      }
      return null;
    } catch {
      // Safe fallback if localStorage access is blocked (e.g., privacy settings or security sandboxes)
      return null;
    }
  }

  /**
   * Resolves the initial locale by evaluating localStorage, falling back to browser language sniffing.
   *
   * @returns resolved initial locale ('en' or 'ja')
   */
  resolveInitialLocale(): SupportedLocale {
    const stored = this.getStoredLocale();
    if (stored !== null) {
      return stored;
    }

    try {
      const browserLang = (typeof navigator !== 'undefined' && navigator.language) ? navigator.language.toLowerCase() : '';
      if (browserLang.startsWith('ja')) {
        return 'ja';
      }
    } catch {
      // Safe fallback on unreadable navigator
    }

    return LocaleService.DEFAULT_LOCALE;
  }

  /**
   * Sets the user's preferred locale, persists it into localStorage, and navigates to the target locale base Href.
   *
   * @param locale the target locale code matching '^[a-z]{2}$' ('en' or 'ja')
   * @throws Error if the provided locale violates the 2-letter format or is unsupported
   */
  setLocale(locale: SupportedLocale): void {
    if (
      typeof locale !== 'string' ||
      !LocaleService.LOCALE_CODE_REGEX.test(locale) ||
      !(LocaleService.SUPPORTED_LOCALES as readonly string[]).includes(locale)
    ) {
      throw new Error(`Unsupported locale: "${locale}". Supported locales are: ${LocaleService.SUPPORTED_LOCALES.join(', ')}`);
    }

    if (typeof localStorage !== 'undefined') {
      try {
        localStorage.setItem(LocaleService.STORAGE_KEY, locale);
      } catch {
        // Non-fatal error; proceed with navigation even if localStorage fails
      }
    }

    if (locale !== this.getActiveLocale()) {
      if (this.document.location) {
        this.document.location.href = `/${locale}/`;
      }
    }
  }
}
