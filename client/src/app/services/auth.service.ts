import {inject, Injectable} from '@angular/core';
import {BehaviorSubject, catchError, map, Observable, throwError} from 'rxjs';
import {User} from '../models/User/user';
import {HttpClient} from '@angular/common/http';

@Injectable({providedIn: 'root'})
export class AuthService {

  // BehaviorSubject to track currently logged in user
  // BehaviourSubject is a special container that holds and gives an updated current value
  private currentUserSubject: BehaviorSubject<User | null>;
  // Observable that components can subscribe to
  public currentUser$: Observable<User | null>;

  /* Dependency Injection */
  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('currentUser');
    console.log("AuthService Initialized, Stored User from localStorage:", storedUser);  // ✅ Debug log

    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );

    this.currentUser$ = this.currentUserSubject.asObservable(); // ✅ Assign observable here

    this.currentUser$.subscribe(user => {
      console.log("Current User Updated in BehaviorSubject:", user); // ✅ Log updates
    });
  }

  /* LOGIN Method for authentication with backend */
  login(email: string, password: string): Observable<User> {
    return this.http.post<any>('/api/auth/login', {email, password})
      .pipe(
        map(response => {
          // Handle string responses
          // Parse string responses into JsonObject
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          // Check if login was successful and has a JWT token
          // === must have same value and type
          if (data.status === 'success' && data.token) {
            // Create user object from response
            const user = {
              email: data.email,
              token: data.token
            };
            console.log("Saving token in localStorage:", data.token);
            // Store user details in localStorage for persistence across page refreshes
            // Local storage can only store strings
            // Store both the token and the user object
            localStorage.setItem('jwtToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify(user));
            const storedUser = localStorage.getItem('currentUser');
            const token = localStorage.getItem('jwtToken');
            console.log("Stored User:", storedUser);
            console.log("Stored Token:", token);
            // Update the BehaviorSubject with the new user
            this.currentUserSubject.next(user);
            return user;
          } else {
            throw new Error(data.message || 'Login failed');
          }
        }),
        catchError(error => {
          return throwError(() => error);
        })
      );
  }

  /* LOGOUT Method */
  logout(): void {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('currentUser');
    // Updates the BehaviorSubject with null value
    this.currentUserSubject.next(null);
  }

  /* Helper Method */

  // Get current user value
  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    // double !! converts value into a boolean
    return !!this.currentUserValue;
  }

  /* Register Service Method */
  register(email: string, password: string): Observable<any> {
    return this.http.post<any>('/api/auth/register', {email, password})
      .pipe(
        map(response => {
          // Handle string responses from backend
          const data = typeof response === 'string' ? JSON.parse(response) : response;

          if (data.status === 'success') {
            return data;
          } else {
            throw new Error(data.message || 'Registration failed');
          }
        }),
        catchError(error => {
          return throwError(() => error);
        })
      );
  }

}
