import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LocaleService, type SupportedLocale } from '../../services/locale.service';

/**
 * Presentation and user interaction component for switching application locale.
 * Conforms to the language switcher contract and delegates state persistence and navigation to LocaleService.
 */
@Component({
  selector: 'app-language-switch',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './language-switch.component.html',
  styleUrl: './language-switch.component.scss',
})
export class LanguageSwitchComponent implements OnInit {
  private readonly localeService = inject(LocaleService);

  /**
   * Signal reflecting the active application locale.
   */
  readonly currentLocale = signal<SupportedLocale>(this.localeService.getActiveLocale());

  /**
   * Synchronizes the signal with the active locale upon component initialization.
   */
  ngOnInit(): void {
    this.currentLocale.set(this.localeService.getActiveLocale());
  }

  /**
   * Toggles the application locale between English and Japanese.
   */
  toggleLocale(): void {
    const nextLocale: SupportedLocale = this.currentLocale() === 'ja' ? 'en' : 'ja';
    this.switchLocale(nextLocale);
  }

  /**
   * Switches the application locale to the specified target locale.
   *
   * @param target the target supported locale ('en' or 'ja')
   * @throws Error if the target locale is not supported
   */
  switchLocale(target: SupportedLocale): void {
    if (target !== 'en' && target !== 'ja') {
      throw new Error(`Unsupported target locale: "${target}". Supported locales are: en, ja`);
    }
    this.localeService.setLocale(target);
  }
}
