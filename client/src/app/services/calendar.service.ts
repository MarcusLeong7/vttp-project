import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {

  // Dependency Injection
  private http = inject(HttpClient);

  // Start the Google OAuth flow
  initiateGoogleAuth(): void {
    // Use the client ID you registered in Google Cloud Console
    const clientId = 'YOUR_GOOGLE_CLIENT_ID';
    // This must match the authorized redirect URI in Google Cloud Console
    const redirectUri = window.location.origin + '/calendar/callback';
    const scope = 'https://www.googleapis.com/auth/calendar';

    // Construct the authorization URL
    const authUrl = `https://accounts.google.com/o/oauth2/auth?` +
      `client_id=${clientId}&` +
      `redirect_uri=${redirectUri}&` +
      `response_type=code&` +
      `scope=${scope}&` +
      `access_type=offline&` +
      `prompt=consent`;

    // Redirect the user to Google's authorization page
    window.location.href = authUrl;
  }

  // Exchange the auth code for tokens via your backend
  connectToGoogle(authCode: string): Observable<any> {
    return this.http.post<any>('/api/calendar/connect', {code: authCode});
  }

  // Add a meal plan to Google Calendar
  addMealPlanToCalendar(mealPlanId: string): Observable<any> {
    return this.http.post<any>(`/api/calendar/mealplan/${mealPlanId}`, {});
  }

  // Check if user is connected to Google Calendar
  checkGoogleConnection(): Observable<any> {
    return this.http.get<any>('/api/calendar/status');
  }

  // Get user's calendar events
  getCalendarEvents(): Observable<any[]> {
    return this.http.get<any[]>('/api/calendar/events');
  }

  // Get the user's primary calendar embed URL
  getCalendarEmbedUrl(): Observable<any> {
    return this.http.get<any>('/api/calendar/embed-url');
  }
}
