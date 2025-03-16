import {inject, Injectable} from '@angular/core';
import {MatNavList} from '@angular/material/list';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CalendarService} from './calendar.service';
import {Router} from '@angular/router';
import {BehaviorSubject, catchError, map, Observable, of, tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PremiumService {

  private isPremiumSubject = new BehaviorSubject<boolean>(false);
  public isPremium$ = this.isPremiumSubject.asObservable();

  constructor(private http: HttpClient) {
    this.checkPremiumStatus();
  }

  public checkPremiumStatus(): Observable<boolean> {
    return this.http.get<any>('/api/user/premium-status').pipe(
      map(response => {
        // Parse the response if it's a string
        const data = typeof response === 'string' ? JSON.parse(response) : response;
        // Extract the boolean value
        const isPremium = data.isPremium === true;
        // Update the subject
        this.isPremiumSubject.next(isPremium);
        // Return the value
        return isPremium;
      }),
      catchError(error => {
        console.error('Error checking premium status:', error);
        this.isPremiumSubject.next(false);
        return of(false);
      })
    );
  }

  public redirectToUpgrade(): void {
    // Can be called from any component
    window.location.href = '/upgrade';
  }
}
