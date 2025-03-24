import {Component, inject, OnInit} from '@angular/core';
import {CalendarService} from '../../services/calendar.service';
import {PremiumService} from '../../services/premium.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';

@Component({
  selector: 'app-schedule',
  standalone: false,
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.css'
})
export class ScheduleComponent implements OnInit {

  // Dependency Injection
  private calendarSvc = inject(CalendarService);
  private premiumSvc = inject(PremiumService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);


  calendarEvents: any[] = [];
  isLoading = false;
  isConnectedToGoogle = false;
  errorMessage = '';
  userEmail = '';
  calendarEmbedUrl: any;
  isPremium = false;
  showUpgradePrompt = false;

  ngOnInit(): void {
    this.isLoading = true;

    // Actively check premium status and wait for the result
    this.premiumSvc.checkPremiumStatus().subscribe({
      next: (isPremium) => {
        this.isPremium = isPremium;

        // Only proceed with calendar operations if premium
        if (isPremium) {
          this.checkGoogleConnection();
          // Don't check isConnectedToGoogle here - do it in the checkGoogleConnection callback
        } else {
          this.showUpgradePrompt = true;
          this.isLoading = false;
        }
      },
      error: (err) => {
        console.error('Error checking premium status:', err);
        this.isLoading = false;
        this.errorMessage = 'Failed to verify premium status.';
      }
    });
  }

  checkGoogleConnection(): void {
    this.isLoading = true;
    this.calendarSvc.getCalendarEmbedUrl().subscribe({
      next: (response) => {
        this.isConnectedToGoogle = true;
        this.calendarEmbedUrl = response.embedUrl;
        this.isLoading = false;
        // When connection is successful, load events
        this.loadCalendarEvents();
      },
      error: (err) => {
        this.isConnectedToGoogle = false;
        this.isLoading = false;
        this.errorMessage = 'Unable to load calendar. Please try reconnecting to Google Calendar.';
      }
    });
  }

  connectToGoogle(): void {
    // Check premium before allowing connection
    if (!this.isPremium) {
      this.snackBar.open('Google Calendar integration is a premium feature.', 'Upgrade', {
        duration: 5000
      }).onAction().subscribe(() => {
        this.router.navigate(['/upgrade']);
      });
      return;
    }
    this.calendarSvc.initiateGoogleAuth();
  }

  loadCalendarEvents(): void {
    this.isLoading = true;
    this.calendarSvc.getCalendarEvents().subscribe({
      next: (events) => {
        console.log('Calendar events loaded:', events);
        this.calendarEvents = events;
        this.isLoading = false;
        console.log('Calendar events loaded:', events);
      },
      error: (err) => {
        console.error('Error loading calendar events:', err);
        this.errorMessage = 'Failed to load calendar events.';
        this.isLoading = false;
      }
    });
  }

  navigateToUpgrade(): void {
    this.router.navigate(['/upgrade']);
  }

  protected readonly encodeURIComponent = encodeURIComponent;
}
