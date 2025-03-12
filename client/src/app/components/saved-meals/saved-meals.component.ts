import {Component, inject, OnInit} from '@angular/core';
import {MealService} from '../../services/meal.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Meal, NutritionInfo} from '../../models/Meal';
import {MealStore} from '../../stores/meal.store';

@Component({
  selector: 'app-saved-meals',
  standalone: false,
  templateUrl: './saved-meals.component.html',
  styleUrl: './saved-meals.component.css'
})
export class SavedMealsComponent implements OnInit {

  // Dependency Injections
  private mealService = inject(MealService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private mealStore = inject(MealStore);

  // Observable streams from the store
  savedMeals$ = this.mealStore.savedMeals$;
  selectedMeals$ = this.mealStore.selectedMeals$;
  nutritionSummary$ = this.mealStore.nutritionSummary$;
  isLoading$ = this.mealStore.isLoading$;
  error$ = this.mealStore.error$;

  // Form for creating meal plan
  mealPlanForm: FormGroup;

  constructor() {
    this.mealPlanForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    // Load saved meals when component initializes
    this.mealStore.loadSavedMeals();

    // Subscribe to changes in selected meals to calculate nutrition
    this.mealStore.selectedMeals$.subscribe(
      meals => {
        if (meals.length > 0) {
          this.mealStore.calculateNutrition(meals);
        }
      }
    );
  }

  // Toggle meal selection
  toggleMealSelection(meal: Meal): void {
    this.mealStore.toggleMealSelection(meal);
  }

  // Check if a meal is selected
  isMealSelected(mealId: string): boolean {
    let isSelected = false;
    this.selectedMeals$.subscribe(meals => {
      isSelected = meals.some(m => m.id === mealId);
    }).unsubscribe();
    return isSelected;
  }

  // Delete Meal
  deleteMeal(mealId: string, event: Event): void {
    // Prevent the click from also selecting the meal
    event.stopPropagation();

    // Confirm deletion
    if (confirm('Are you sure you want to delete this meal?')) {
      this.mealStore.deleteMeal(mealId);

      // Show a snackbar notification
      this.snackBar.open('Meal deleted successfully', 'Close', {
        duration: 3000
      });
    }
  }

  // Create a meal plan
  createMealPlan(): void {
    if (this.mealPlanForm.invalid) {
      return;
    }

    let selectedMeals: Meal[] = [];
    this.selectedMeals$.subscribe(meals => {
      selectedMeals = meals;
    }).unsubscribe();

    if (selectedMeals.length === 0) {
      this.snackBar.open('Please select at least one meal for your meal plan', 'Close', {
        duration: 3000
      });
      return;
    }

    const mealPlanData = {
      ...this.mealPlanForm.value,
      meals: selectedMeals
    };

    // We'll implement the actual save functionality later
    console.log('Creating meal plan:', mealPlanData);
    this.snackBar.open('Meal plan created successfully!', 'Close', {
      duration: 3000
    });

    this.mealPlanForm.reset();
  }
}
