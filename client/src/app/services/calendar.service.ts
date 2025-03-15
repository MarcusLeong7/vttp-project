import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, map, Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {

  // Dependency Injection
  private http = inject(HttpClient);

// Start the Google OAuth flow
  initiateGoogleAuth(): void {
    // Use the client ID you registered in Google Cloud Console
    const clientId = '949519111790-96f87b032ij89kuo4tq62vlppb2d00er.apps.googleusercontent.com';
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
    // Add more detailed error handling
    return this.http.post<any>(`/api/calendar/mealplan/${mealPlanId}`, {})
      .pipe(
        catchError(error => {
          console.error('Calendar API error:', error);
          if (error.status === 0) {
            console.error('Network error or CORS issue');
          } else if (error.status === 401) {
            console.error('Authentication issue - user might need to connect to Google');
          } else {
            console.error('Server error:', error.error?.message || error.statusText);
          }
          return throwError(() => error);
        })
      );
  }

  // Check if user is connected to Google Calendar
  checkGoogleConnection(): Observable<any> {
    return this.http.get<any>('/api/calendar/status');
  }

  // Get the user's primary calendar embed URL
  getCalendarEmbedUrl(): Observable<any> {
    return this.http.get<any>('/api/calendar/embed-url');
  }

  // Get user's calendar events
  getCalendarEvents(): Observable<any[]> {
    console.log('Fetching calendar events');
    return this.http.get<any>('/api/calendar/events').pipe(
      map(response => {
        console.log('Raw calendar response:', response);
        // Handle both string and object responses
        const data = typeof response === 'string' ? JSON.parse(response) : response;
        return data;
      }),
      catchError(error => {
        console.error('Error fetching calendar events:', error);
        return throwError(() => error);
      })
    );
  }

}
