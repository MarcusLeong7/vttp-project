import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  protected registerForm: FormGroup = this.fb.group({
    email: ['', [Validators.required,
      Validators.email,
      Validators.pattern(/^[\w\.-]+@[\w\.-]+\.[a-z]{2,}$/)]],
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      // Password pattern validation: at least one uppercase, one lowercase, one number, and one special character
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
    ]],
    confirmPassword: ['', [Validators.required]]
  }, {
    validators: this.passwordMatchValidator
  });

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Custom validator to ensure passwords match
  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({passwordMismatch: true});
      return {passwordMismatch: true};
    }

    return null;
  }

  protected submit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const {email, password} = this.registerForm.value;

    this.authService.register(email, password)
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Registration successful! You can now log in.';

          // Reset the form
          this.registerForm.reset();

          // After a short delay, redirect to login page
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.message || 'Registration failed. Please try again.';
        }
      });
  }

  // Helper methods for template access
  get emailControl() {
    return this.registerForm.get('email');
  }

  get passwordControl() {
    return this.registerForm.get('password');
  }

  get confirmPasswordControl() {
    return this.registerForm.get('confirmPassword');
  }
}
