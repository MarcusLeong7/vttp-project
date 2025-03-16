import {Component, inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {PaymentService} from '../../services/payment.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {firstValueFrom} from 'rxjs';

declare var Stripe: any;

@Component({
  selector: 'app-upgrade',
  standalone: false,
  templateUrl: './upgrade.component.html',
  styleUrl: './upgrade.component.css'
})
export class UpgradeComponent implements OnInit {

  // Dependency Injections
  private paymentSvc = inject(PaymentService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  // Component properties
  loading = false;
  stripe: any;
  card: any;
  errorMessage = '';
  selectedPlan = 'premium'; // Default plan

  ngOnInit(): void {
    this.paymentSvc.getStripeConfig().subscribe(
      (response) => {
        this.stripe = Stripe(response.publicKey);
        const elements = this.stripe.elements();
        this.card = elements.create('card');
        this.card.mount('#card-element');

        // Handle card validation errors
        this.card.on('change', (event: any) => {
          const displayError = document.getElementById('card-errors');
          if (event.error && displayError) {
            displayError.textContent = event.error.message;
          } else if (displayError) {
            displayError.textContent = '';
          }
        });
      },
      (error) => {
        this.errorMessage = 'Failed to load payment processor';
        console.error('Error getting Stripe config:', error);
      }
    );
  }

  async handleSubmit() {
    this.loading = true;

    try {
      // Create payment intent on the server
      const response = await firstValueFrom(this.paymentSvc.createPaymentIntent(this.selectedPlan));

      // Confirm card payment
      const result = await this.stripe.confirmCardPayment(response.clientSecret, {
        payment_method: {
          card: this.card,
          billing_details: {
            // You can collect these details from the user if needed
          }
        }
      });

      if (result.error) {
        // Show error message
        this.errorMessage = result.error.message;
        this.loading = false;
      } else {
        if (result.paymentIntent.status === 'succeeded') {
          // Payment successful
          await this.paymentSvc.confirmPaymentSuccess().toPromise();
          this.snackBar.open('Upgrade successful! You now have premium access.', 'Close', {
            duration: 5000
          });
          this.router.navigate(['/home']);
        }
      }
    } catch (error) {
      console.error('Payment error:', error);
      this.errorMessage = 'An error occurred during payment processing';
    } finally {
      this.loading = false;
    }
  }

  selectPlan(plan: string) {
    this.selectedPlan = plan;
  }
}
