import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({providedIn: 'root'})
export class MealPlanService {

  private http = inject(HttpClient);

  // Get all meal plans for the current user
  getMealPlans(): Observable<any[]> {
    return this.http.get<any>('/api/mealplans')
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Get a specific meal plan by ID
  getMealPlanById(id: string): Observable<any> {
    return this.http.get<any>(`/api/mealplans/${id}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Create a new meal plan
  createMealPlan(planData: any): Observable<any> {
    return this.http.post<any>('/api/mealplans', planData)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Update an existing meal plan
  updateMealPlan(id: string, planData: any): Observable<any> {
    return this.http.put<any>(`/api/mealplans/${id}`, planData)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }
  // Delete a meal plan
  deleteMealPlan(id: string): Observable<any> {
    return this.http.delete<any>(`/api/mealplans/${id}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

}
