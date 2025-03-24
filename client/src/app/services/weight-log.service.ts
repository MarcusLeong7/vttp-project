import {inject, Injectable} from "@angular/core";
import {HttpClient} from '@angular/common/http';
import {WeightLog} from '../models/User/user';
import {map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WeightLogService {

  private http = inject(HttpClient);

  // Get weight logs for the last N days
  getWeightLogs(days: number = 30): Observable<WeightLog[]> {
    return this.http.get<any>(`/api/user/weight-logs?days=${days}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data.logs;
        })
      );
  }

  // Add a new weight log
  addWeightLog(weight: number, notes?: string): Observable<WeightLog> {
    return this.http.post<any>('/api/user/weight-logs', { weight, notes })
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data.log;
        })
      );
  }

  // Update an existing weight log
  updateWeightLog(id: number, weight: number, notes?: string): Observable<WeightLog> {
    return this.http.put<any>(`/api/user/weight-logs/${id}`, { weight, notes })
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data.log;
        })
      );
  }

  // Delete a weight log
  deleteWeightLog(id: number): Observable<any> {
    return this.http.delete<any>(`/api/user/weight-logs/${id}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }
}
