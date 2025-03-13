import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MealPlanService} from '../../services/meal.plan.service';

@Component({
  selector: 'app-meal-plan-list',
  standalone: false,
  templateUrl: './meal-plan-list.component.html',
  styleUrl: './meal-plan-list.component.css'
})
export class MealPlanListComponent implements OnInit {

  // Inject dependencies
  private router = inject(Router)
  private snackBar = inject(MatSnackBar)
  private mealPlanService = inject(MealPlanService)

  // Component properties
  mealPlans: any[] = [];
  isLoading = false;
  error: string | null = null;

  // Day names for display
  weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

  ngOnInit(): void {
    this.loadMealPlans();
  }

  // Load all meal plans
  loadMealPlans(): void {
    this.isLoading = true;
    this.error = null;

    this.mealPlanService.getMealPlans().subscribe({
      next: (plans) => {
        this.mealPlans = plans;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading meal plans:', err);
        this.error = 'Failed to load meal plans. Please try again.';
        this.isLoading = false;
      }
    });
  }

  // View meal plan details
  viewMealPlan(id: string): void {
    this.router.navigate(['/meal-plans', id]);
  }

  // Delete a meal plan
  deleteMealPlan(id: string, event: Event): void {
    event.stopPropagation(); // Prevent viewMealPlan from being triggered

    if (confirm('Are you sure you want to delete this meal plan? This cannot be undone.')) {
      this.isLoading = true;

      this.mealPlanService.deleteMealPlan(id).subscribe({
        next: () => {
          this.snackBar.open('Meal plan deleted successfully', 'Close', {
            duration: 3000
          });
          this.loadMealPlans(); // Reload the list
        },
        error: (err) => {
          console.error('Error deleting meal plan:', err);
          this.snackBar.open('Failed to delete meal plan', 'Close', {
            duration: 3000
          });
          this.isLoading = false;
        }
      });
    }
  }

  // Get day name from day number
  getDayName(dayOfWeek: number | null): string {
    if (dayOfWeek === null || dayOfWeek === undefined) {
      return 'Any day';
    }
    return this.weekdays[dayOfWeek];
  }

  // Get total calories for a meal plan
  getTotalCalories(mealPlan: any): number {
    return mealPlan.items.reduce((sum: number, item: any) => {
      return sum + (parseInt(item.calories) || 0);
    }, 0);
  }

  // Navigate to saved meals page to create new plan
  createNewPlan(): void {
    this.router.navigate(['/meals/saved']);
  }

}
