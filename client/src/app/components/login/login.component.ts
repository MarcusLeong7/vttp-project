import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  // Dependency Injections
  private fb = inject(FormBuilder);
  private authSvc = inject(AuthService);
  private router = inject(Router);

  protected form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    })
  }

  protected isLoading=false;
  protected errorMessage = ""

  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    // Used to temporarily disable login button when authentication is being done
    this.isLoading = true;
    this.errorMessage = '';

    const { email, password } = this.form.value;

    this.authSvc.login(email, password)
      .subscribe({
        next: () => {
          this.isLoading = false;
          // Navigate to the home route on successful login
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = 'Invalid email or password';
        }
      });
  }
}
