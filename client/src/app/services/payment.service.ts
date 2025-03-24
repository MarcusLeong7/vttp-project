import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private http = inject(HttpClient)

  getStripeConfig(): Observable<any> {
    return this.http.get('/api/payment/config');
  }

  createCheckoutSession(priceId: string): Observable<any> {
    return this.http.post('/api/payment/create-checkout-session', { priceId });
  }

  verifyPaymentSuccess(sessionId: string): Observable<any> {
    return this.http.get(`/api/payment/verify-payment?session_id=${sessionId}`);
  }


}
