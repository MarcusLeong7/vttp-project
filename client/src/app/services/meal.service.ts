import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Meal, MealSearchParams, NutritionInfo} from '../models/Meal/Meal';
import {map, Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class MealService {

  // Inject HttpClient
  private http = inject(HttpClient)

  // Get meals with optional filters
  getMeals(searchParams?: MealSearchParams): Observable<Meal[]> {
    let params = new HttpParams();
    if (searchParams) {
      if (searchParams.maxCalories) {
        params = params.set('maxCalories', searchParams.maxCalories.toString());
      }
      if (searchParams.minProtein) {
        params = params.set('minProtein', searchParams.minProtein);
      }
      if (searchParams.maxCarbs) {
        params = params.set('maxCarbs', searchParams.maxCarbs);
      }
      if (searchParams.maxFats) {
        params = params.set('maxFats', searchParams.maxFats);
      }
    }
    return this.http.get<any>('api/meals/search', {params})
      .pipe(
        map(response => {
          // Handle string response
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data as Meal[];
        })
      );
  }

  // Get recipe URL for a meal
  getRecipeUrl(mealId: string): Observable<string> {
    return this.http.get<any>(`api/meals/${mealId}/recipe`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data.recipeUrl;
        })
      );
  }

  // Save Meals
  saveMeals(meals: Meal[]): Observable<any> {
    return this.http.post<any>('/api/meals/save', { meals })
      .pipe(
        map(response => {
          // Handle both string and object responses
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Delete a saved meal for User
  deleteSavedMeal(mealId: string): Observable<any> {
    return this.http.delete<any>(`/api/meals/saved/${mealId}`)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Get all saved meals for the current user
  getSavedMeals(): Observable<Meal[]> {
    return this.http.get<any>('/api/meals/saved')
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data as Meal[];
        })
      );
  }

  // Calculate nutrition summary for selected meals
  calculateNutritionSummary(meals: Meal[]): Observable<NutritionInfo> {
    return this.http.post<any>('/api/meals/nutrition-summary', { meals })
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return {
            calories: data.totalCalories,
            protein: data.totalProtein,
            carbs: data.totalCarbs,
            fats: data.totalFats
          } as NutritionInfo;
        })
      );
  }



}
