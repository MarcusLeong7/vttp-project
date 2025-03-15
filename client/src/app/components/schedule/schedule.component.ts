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


  ngOnInit(): void {
    this.checkGoogleConnection();
    this.loadCalendarEvents();
  }

  checkGoogleConnection(): void {
    this.isLoading = true;
    this.calendarSvc.checkGoogleConnection().subscribe({
      next: (response) => {
        this.isConnectedToGoogle = response.connected;
        this.isLoading = false;
      },
      error: (err) => {
        this.isConnectedToGoogle = false;
        this.isLoading = false;
      }
    });
  }

  loadCalendarEvents(): void {
    if (!this.isConnectedToGoogle) return;

    this.isLoading = true;
    this.calendarSvc.getCalendarEvents().subscribe({
      next: (events) => {
        this.calendarEvents = events;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load calendar events. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  connectToGoogle(): void {
    this.calendarSvc.initiateGoogleAuth();
  }

}
