import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PaymentService} from '../../services/payment.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-payment-success',
  standalone: false,
  templateUrl: './payment-success.component.html',
  styleUrl: './payment-success.component.css'
})
export class PaymentSuccessComponent implements OnInit {

  // Dependencies
  private route = inject(ActivatedRoute);
  private paymentSvc = inject(PaymentService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  // Properties
  isVerifying = true;
  error = false;

  ngOnInit(): void {
    // Get session ID from URL
    this.route.queryParams.subscribe(params => {
      const sessionId = params['session_id'];

      if (!sessionId) {
        this.error = true;
        this.isVerifying = false;
        return;
      }

      // Verify the payment with backend
      this.paymentSvc.verifyPaymentSuccess(sessionId).subscribe({
        next: (response) => {
          this.isVerifying = false;
          // Show success message
          this.snackBar.open('Upgrade successful! You now have premium access.', 'Close', {
            duration: 5000
          });
          // Redirect after a brief delay to show the success page
          setTimeout(() => {
            this.router.navigate(['/home']);
          }, 3000);
        },
        error: (err) => {
          this.isVerifying = false;
          this.error = true;
          console.error('Payment verification error:', err);
          this.snackBar.open('There was an issue verifying your payment.', 'Close', {
            duration: 5000
          });
        }
      });
    });
  }
}
