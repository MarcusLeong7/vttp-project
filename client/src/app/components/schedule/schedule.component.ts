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

  ngOnInit(): void {
    this.checkGoogleConnection();
  }

  checkGoogleConnection(): void {
    this.isLoading = true;
    this.calendarSvc.checkGoogleConnection().subscribe({
      next: (response) => {
        this.isConnectedToGoogle = response.connected;
        this.userEmail = response.email || '';
        this.isLoading = false;
      },
      error: (err) => {
        this.isConnectedToGoogle = false;
        this.isLoading = false;
      }
    });
  }

  connectToGoogle(): void {
    this.calendarSvc.initiateGoogleAuth();
  }

}
