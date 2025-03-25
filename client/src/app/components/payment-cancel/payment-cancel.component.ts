import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'app-payment-cancel',
  standalone: false,
  templateUrl: './payment-cancel.component.html',
  styleUrl: './payment-cancel.component.css'
})
export class PaymentCancelComponent {

  private router = inject(Router)

  redirectToUpgrade(): void {
    this.router.navigate(['/upgrade']);
  }
  redirectToHome(): void {
    this.router.navigate(['/home']);
  }

}
