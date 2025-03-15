import {Component, inject} from '@angular/core';
import {CalendarService} from '../../services/calendar.service';

@Component({
  selector: 'app-schedule',
  standalone: false,
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.css'
})
export class ScheduleComponent {

  // Dependency Injection
  private calendarSvc = inject(CalendarService);

  calendarEvents: any[] = [];
  isLoading = false;
  isConnectedToGoogle = false;
  errorMessage = '';
  userEmail = '';
   calendarEmbedUrl: any;

  ngOnInit(): void {
    this.checkGoogleConnection();
    if (this.isConnectedToGoogle) {
      this.loadCalendarEvents();
    }
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

  protected readonly encodeURIComponent = encodeURIComponent;
}
