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
  errorMessage = '';
  selectedPlan = 'premium'; // Default plan

  ngOnInit(): void {
    // Get Stripe public key
    this.paymentSvc.getStripeConfig().subscribe(
      (response) => {
        // Store the public key if needed
        console.log('Stripe configuration loaded');
      },
      (error) => {
        this.errorMessage = 'Failed to load payment processor';
        console.error('Error getting Stripe config:', error);
      }
    );
  }

  handleCheckout() {
    this.loading = true;

    this.paymentSvc.createCheckoutSession(this.selectedPlan).subscribe(
      (response) => {
        // Redirect to Stripe Checkout
        window.location.href = response.url;
      },
      (error) => {
        this.loading = false;
        this.errorMessage = 'Error creating checkout session: ' + (error.message || 'Unknown error');
        console.error('Payment error:', error);
      }
    );
  }

  selectPlan(plan: string) {
    this.selectedPlan = plan;
  }
}
