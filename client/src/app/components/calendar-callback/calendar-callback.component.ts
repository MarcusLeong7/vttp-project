import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CalendarService} from '../../services/calendar.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-calendar-callback',
  standalone: false,
  templateUrl: './calendar-callback.component.html',
  styleUrl: './calendar-callback.component.css'
})
export class CalendarCallbackComponent implements OnInit {

  // Dependency Injections
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private calendarSvc = inject(CalendarService);

  ngOnInit(): void {
    // Extract the authorization code from the URL
    this.activatedRoute.queryParams.subscribe(params => {
      const code = params['code'];

      if (code) {
        // Exchange the code for tokens
        this.calendarSvc.connectToGoogle(code).subscribe({
          next: () => {
            this.snackBar.open('Successfully connected to Google Calendar', 'Close', {
              duration: 3000
            });
            // Navigate back to the meal plans page
            this.router.navigate(['/meal-plans']);
          },
          error: (err) => {
            console.error('Failed to connect to Google Calendar:', err);
            this.snackBar.open('Failed to connect to Google Calendar', 'Close', {
              duration: 3000
            });
            this.router.navigate(['/meal-plans']);
          }
        });
      } else {
        // No code was provided
        this.snackBar.open('No authorization code received from Google', 'Close', {
          duration: 3000
        });
        this.router.navigate(['/meal-plans']);
      }
    });
  }
}
