import { Component, inject, output, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PostApiService } from '../../services/post-api.service';
import { CreatePostRequest } from '../../models/post.model';

/**
 * Validates that a string form control does not consist entirely of whitespace.
 *
 * @returns validator function returning `{ whitespace: true }` when blank
 */
export function nonBlankValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }
    return (control.value + '').trim().length === 0 ? { whitespace: true } : null;
  };
}

/**
 * Validates email format only when a non-empty string is entered,
 * allowing blank or whitespace values for optional email input.
 *
 * @returns validator function returning standard email validation errors if invalid
 */
export function optionalEmailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value === null || value === undefined || (value + '').trim().length === 0) {
      return null;
    }
    const trimmed = (value + '').trim();
    return Validators.email({ value: trimmed } as AbstractControl);
  };
}

/**
 * Persistent bottom-anchored form component for submitting new bulletin board messages.
 */
@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './post-form.component.html',
  styleUrl: './post-form.component.scss',
})
export class PostFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly postApiService = inject(PostApiService);
  private readonly snackBar = inject(MatSnackBar);

  /** Emitted when a post has been successfully persisted by the backend API. */
  readonly postCreated = output<void>();

  /** Indicates whether a submission request is currently in-flight. */
  readonly isSubmitting = signal<boolean>(false);

  /** Reactive form group defining bulletin board input controls and constraints. */
  readonly postForm = this.fb.group({
    name: ['', [Validators.required, nonBlankValidator(), Validators.maxLength(50)]],
    email: ['', [optionalEmailValidator(), Validators.maxLength(254)]],
    title: ['', [Validators.required, nonBlankValidator(), Validators.maxLength(100)]],
    message: ['', [Validators.required, nonBlankValidator(), Validators.maxLength(4000)]],
  });

  /**
   * Validates input values and submits the new post to the backend API.
   * On validation failure, retains entered values and displays validation errors.
   */
  onSubmit(): void {
    if (this.isSubmitting()) {
      return;
    }

    if (this.postForm.invalid) {
      this.postForm.markAllAsTouched();
      return;
    }

    const formValues = this.postForm.getRawValue();
    const trimmedName = (formValues.name ?? '').trim();
    const rawEmail = (formValues.email ?? '').trim();
    const normalizedEmail = rawEmail.length > 0 ? rawEmail : null;
    const trimmedTitle = (formValues.title ?? '').trim();
    const trimmedMessage = (formValues.message ?? '').trim();

    const requestPayload: CreatePostRequest = {
      name: trimmedName,
      email: normalizedEmail,
      title: trimmedTitle,
      message: trimmedMessage,
    };

    this.isSubmitting.set(true);

    this.postApiService.createPost(requestPayload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.resetForm();
        const successMessage = $localize`:@@app.form.success:Post submitted successfully!`;
        const closeLabel = $localize`:@@app.form.close:Close`;
        this.snackBar.open(successMessage, closeLabel, {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
        });
        this.postCreated.emit();
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        let errorMessage = $localize`:@@app.form.generic_error:An error occurred while submitting the post. Please try again later.`;
        if (err && typeof err === 'object') {
          const problemDetail = (err as { error?: { detail?: string } }).error?.detail;
          if (problemDetail && typeof problemDetail === 'string' && problemDetail.trim().length > 0) {
            errorMessage = problemDetail.trim();
          }
        }

        const closeLabel = $localize`:@@app.form.close:Close`;
        this.snackBar.open(errorMessage, closeLabel, {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['snackbar-error'],
        });
      },
    });
  }

  /**
   * Resets all form fields and clears dirty/touched states.
   */
  resetForm(): void {
    this.postForm.reset({
      name: null,
      email: null,
      title: null,
      message: null,
    });
  }
}
