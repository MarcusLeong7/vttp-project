import {Injectable} from '@angular/core';
import {ComponentStore} from '@ngrx/component-store';
import {MealState} from '../models/MealState';
import {MealService} from '../services/meal.service';
import {Meal, NutritionInfo} from '../models/Meal';
import {catchError, EMPTY, Observable, switchMap, tap} from 'rxjs';

// Initial state
const initialState: MealState = {
  savedMeals: [],
  selectedMeals: [],
  nutritionSummary: null,
  isLoading: false,
  error: null
};

@Injectable()
export class MealStore extends ComponentStore<MealState> {
  constructor(private mealService: MealService) {
    super(initialState);
  }

  // SELECTORS
  readonly savedMeals$ = this.select(state => state.savedMeals);
  readonly selectedMeals$ = this.select(state => state.selectedMeals);
  readonly nutritionSummary$ = this.select(state => state.nutritionSummary);
  readonly isLoading$ = this.select(state => state.isLoading);
  readonly error$ = this.select(state => state.error);

  // UPDATERS
  readonly setLoading = this.updater((state, isLoading: boolean) => ({
    ...state,
    isLoading
  }));

  readonly setError = this.updater((state, error: string | null) => ({
    ...state,
    error
  }));

  readonly setSavedMeals = this.updater((state, savedMeals: Meal[]) => ({
    ...state,
    savedMeals,
    isLoading: false
  }));

  readonly toggleMealSelection = this.updater((state, meal: Meal) => {
    const isSelected = state.selectedMeals.some(m => m.id === meal.id);

    let selectedMeals: Meal[];
    if (isSelected) {
      // Remove from selection
      selectedMeals = state.selectedMeals.filter(m => m.id !== meal.id);
    } else {
      // Add to selection
      selectedMeals = [...state.selectedMeals, meal];
    }

    return {
      ...state,
      selectedMeals
    };
  });

  readonly setNutritionSummary = this.updater((state, summary: NutritionInfo | null) => ({
    ...state,
    nutritionSummary: summary
  }));

  readonly removeSavedMeal = this.updater((state, mealId: string) => ({
    ...state,
    savedMeals: state.savedMeals.filter(meal => meal.id !== mealId),
    selectedMeals: state.selectedMeals.filter(meal => meal.id !== mealId)
  }));

  // EFFECTS
  readonly loadSavedMeals = this.effect((trigger$: Observable<void>) => {
    return trigger$.pipe(
      tap(() => {
        this.setLoading(true);
        this.setError(null);
      }),
      switchMap(() => this.mealService.getSavedMeals().pipe(
        tap({
          next: meals => this.setSavedMeals(meals),
          error: error => {
            this.setLoading(false);
            this.setError('Failed to load saved meals');
            console.error('Error loading meals:', error);
          }
        }),
        catchError(() => EMPTY)
      ))
    );
  });

  readonly calculateNutrition = this.effect((meals$: Observable<Meal[]>) => {
    return meals$.pipe(
      switchMap(meals => {
        if (meals.length === 0) {
          this.setNutritionSummary(null);
          return EMPTY;
        }

        return this.mealService.calculateNutritionSummary(meals).pipe(
          tap({
            next: summary => this.setNutritionSummary(summary),
            error: error => console.error('Error calculating nutrition:', error)
          }),
          catchError(() => EMPTY)
        );
      })
    );
  });

  // Effect to delete a meal
  readonly deleteMeal = this.effect((mealId$: Observable<string>) => {
    return mealId$.pipe(
      tap(() => {
        this.setLoading(true);
        this.setError(null);
      }),
      switchMap(mealId => this.mealService.deleteSavedMeal(mealId).pipe(
        tap({
          next: () => {
            this.removeSavedMeal(mealId);
            this.setLoading(false);
          },
          error: error => {
            this.setLoading(false);
            this.setError('Failed to delete meal');
            console.error('Error deleting meal:', error);
          }
        }),
        catchError(() => EMPTY)
      ))
    );
  });
}
