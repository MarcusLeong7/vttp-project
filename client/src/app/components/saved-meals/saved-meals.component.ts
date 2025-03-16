import {Component, inject, OnInit} from '@angular/core';
import {MealService} from '../../services/meal.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Meal} from '../../models/Meal/Meal';
import {MealStore} from '../../stores/meal.store';
import { Router } from '@angular/router';
import { MealPlanService } from '../../services/meal.plan.service';
import {MealType} from '../../models/MealPlan/MealPlan';

@Component({
  selector: 'app-saved-meals',
  standalone: false,
  templateUrl: './saved-meals.component.html',
  styleUrl: './saved-meals.component.css'
})
export class SavedMealsComponent implements OnInit {

  // Dependency Injections
  private mealPlanService = inject(MealPlanService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private mealStore = inject(MealStore);
  private router = inject(Router);

  // Observable streams from the store
  savedMeals$ = this.mealStore.savedMeals$;
  selectedMeals$ = this.mealStore.selectedMeals$;
  nutritionSummary$ = this.mealStore.nutritionSummary$;
  isLoading$ = this.mealStore.isLoading$;
  error$ = this.mealStore.error$;

  // Form for creating meal plan
  mealPlanForm: FormGroup;

  // Day options for select dropdown
  dayOptions = [
    { value: 0, label: 'Sunday' },
    { value: 1, label: 'Monday' },
    { value: 2, label: 'Tuesday' },
    { value: 3, label: 'Wednesday' },
    { value: 4, label: 'Thursday' },
    { value: 5, label: 'Friday' },
    { value: 6, label: 'Saturday' }
  ];

  // Meal types
  mealTypes = Object.values(MealType);

  // Map to store meal type assignments
  mealTypeAssignments = new Map<string, MealType>();

  constructor() {
    this.mealPlanForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      dayOfWeek: [null]
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

    // Initialize meal type to breakfast if not already set
    if (!this.mealTypeAssignments.has(meal.id)) {
      this.mealTypeAssignments.set(meal.id, MealType.BREAKFAST);
    }
  }

  // Check if a meal is selected
  isMealSelected(mealId: string): boolean {
    let isSelected = false;
    this.selectedMeals$.subscribe(meals => {
      isSelected = meals.some(m => m.id === mealId);
    }).unsubscribe();
    return isSelected;
  }

  // Update meal type assignment
  updateMealType(mealId: string, type: MealType): void {
    this.mealTypeAssignments.set(mealId, type);
  }

  // Get the assigned meal type
  getMealType(mealId: string): MealType {
    return this.mealTypeAssignments.get(mealId) || MealType.BREAKFAST;
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

    // Prepare meal items with type assignments
    const mealItems = selectedMeals.map(meal => ({
      mealId: meal.id,
      mealTitle: meal.title,
      mealImage: meal.image,
      calories: meal.calories,
      protein: meal.protein,
      carbs: meal.carbs,
      fats: meal.fats,
      mealType: this.getMealType(meal.id)
    }));

    // Prepare meal plan data
    const mealPlanData = {
      ...this.mealPlanForm.value,
      meals: mealItems
    };

    // Create meal plan via service
    this.mealPlanService.createMealPlan(mealPlanData).subscribe({
      next: (response) => {
        this.snackBar.open('Meal plan created successfully!', 'Close', {
          duration: 3000
        });
        this.mealPlanForm.reset();
        this.router.navigate(['/meal-plans']);
      },
      error: (err) => {
        // Check if this is a premium upgrade error
        if (err.status === 403 && err.error?.requiresUpgrade) {
          this.snackBar.open('You\'ve reached the limit for free accounts.', 'Upgrade', {
            duration: 5000
          }).onAction().subscribe(() => {
            this.router.navigate(['/upgrade']);
          });
        } else {
          this.snackBar.open('Failed to create meal plan. Please try again.', 'Close', {
            duration: 3000
          });
        }
        console.error('Error creating meal plan:', err);
      }
    });
  }

  // Delete Meal (existing functionality)
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
}
