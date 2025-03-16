import {Component, inject} from '@angular/core';
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
export class ScheduleComponent {

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
    // Check premium status first
    this.premiumSvc.isPremium$.subscribe(isPremium => {
      this.isPremium = isPremium;

      // Only proceed with calendar operations if premium
      if (isPremium) {
        this.checkGoogleConnection();
        if (this.isConnectedToGoogle) {
          this.loadCalendarEvents();
        }
      } else {
        this.showUpgradePrompt = true;
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
