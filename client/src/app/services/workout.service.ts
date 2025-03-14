import {inject, Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {Workout, WorkoutSearchParams} from '../models/Workout/workout';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class WorkoutService {

  // Inject HttpClient
  private http = inject(HttpClient);

  // Search workouts with optional filters
  searchWorkouts(searchParams?: WorkoutSearchParams): Observable<Workout[]> {
    let params = new HttpParams();
    if (searchParams) {
      if (searchParams.force) {
        params = params.set('force', searchParams.force);
      }
      if (searchParams.level) {
        params = params.set('level', searchParams.level);
      }
      if (searchParams.primaryMuscle) {
        params = params.set('primaryMuscle', searchParams.primaryMuscle);
      }
    }

    return this.http.get<any>('api/workouts/search', { params })
      .pipe(
        map(response => {
          // Handle string response
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data as Workout[];
        })
      );
  }

  // Save workouts
  saveWorkouts(workouts: Workout[]): Observable<any> {
    return this.http.post<any>('/api/workouts/save', { workouts })
      .pipe(
        map(response => {
          // Handle both string and object responses
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Delete a saved workout for user
  deleteSavedWorkout(workoutId: string): Observable<any> {
    return this.http.delete<any>(`/api/workouts/saved/${workoutId}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Get all saved workouts for the current user
  getSavedWorkouts(): Observable<Workout[]> {
    return this.http.get<any>('/api/workouts/saved')
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data as Workout[];
        })
      );
  }
}
