import { Component, inject, output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MessageCreateRequest } from '../../models/message.model';

/**
 * Validator verifying that input string does not consist exclusively of whitespace characters.
 *
 * @returns Validator function returning `{ whitespace: true }` when value is blank
 */
export function nonWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const val = control.value;
    if (val === null || val === undefined) {
      return null;
    }
    return typeof val === 'string' && val.trim().length === 0 ? { whitespace: true } : null;
  };
}

// RFC 5322 compatible email regular expression matching Angular's standard email validator
const EMAIL_REGEXP =
  /^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

/**
 * Validator verifying email format only when an email value is present.
 *
 * Automatically trims whitespace before evaluating email format validity.
 *
 * @returns Validator function returning `{ email: true }` when non-empty value fails email format
 */
export function optionalEmailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const val = control.value;
    if (val === null || val === undefined) {
      return null;
    }
    const trimmed = typeof val === 'string' ? val.trim() : '';
    if (trimmed.length === 0) {
      return null;
    }
    return EMAIL_REGEXP.test(trimmed) ? null : { email: true };
  };
}

/**
 * Message input form component.
 *
 * Captures user input for message posting, applies reactive validation,
 * and emits trimmed payload upstream while anchoring to viewport bottom.
 */
@Component({
  selector: 'app-message-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './message-form.component.html',
  styleUrl: './message-form.component.scss',
})
export class MessageFormComponent {
  private readonly fb = inject(FormBuilder);

  /**
   * Event emitted when the form is valid and submitted, containing the trimmed request payload.
   */
  readonly submitMessage = output<MessageCreateRequest>();

  /**
   * Reactive form group managing message creation inputs.
   */
  readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required, nonWhitespaceValidator(), Validators.maxLength(50)]],
    email: ['', [Validators.maxLength(100), optionalEmailValidator()]],
    title: ['', [Validators.required, nonWhitespaceValidator(), Validators.maxLength(100)]],
    message: ['', [Validators.required, nonWhitespaceValidator(), Validators.maxLength(1000)]],
  });

  /**
   * Submits the form if valid, emitting trimmed values to parent container.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;
    const emailRaw = raw.email ? raw.email.trim() : '';

    const payload: MessageCreateRequest = {
      name: raw.name.trim(),
      email: emailRaw.length > 0 ? emailRaw : null,
      title: raw.title.trim(),
      message: raw.message.trim(),
    };

    this.submitMessage.emit(payload);
  }

  /**
   * Resets the form fields and restores pristine validation state.
   */
  resetForm(): void {
    this.form.reset();
  }
}
