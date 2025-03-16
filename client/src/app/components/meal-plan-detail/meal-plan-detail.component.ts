import {Component, inject} from '@angular/core';
import {MealPlanService} from '../../services/meal.plan.service';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CalendarService} from '../../services/calendar.service';
import {PremiumService} from '../../services/premium.service';

@Component({
  selector: 'app-meal-plan-detail',
  standalone: false,
  templateUrl: './meal-plan-detail.component.html',
  styleUrl: './meal-plan-detail.component.css'
})
export class MealPlanDetailComponent {

  // Inject dependencies
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private mealPlanService = inject(MealPlanService);
  private calendarService = inject(CalendarService);
  private premiumService = inject(PremiumService);

  // Component properties
  mealPlan: any = null;
  isLoading = false;
  error: string | null = null;
  isPremium: boolean = false;

  // Day names for display
  weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

  // Meal type groups
  breakfastItems: any[] = [];
  lunchItems: any[] = [];
  dinnerItems: any[] = [];
  snackItems: any[] = [];

  // Get premium status
  ngOnInit(): void {
    // First check premium status
    this.premiumService.checkPremiumStatus().subscribe({
      next: (isPremium) => {
        this.isPremium = isPremium;
        console.log('Premium status:', isPremium);
      },
      error: (err) => {
        console.error('Error checking premium status:', err);
      }
    });

    // Then load meal plan
    this.loadMealPlan();
  }

  // Load meal plan details
  loadMealPlan(): void {
    this.isLoading = true;
    this.error = null;

    const id = this.activatedRoute.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Meal plan ID is required';
      this.isLoading = false;
      return;
    }

    this.mealPlanService.getMealPlanById(id).subscribe({
      next: (mealPlan) => {
        this.mealPlan = mealPlan;
        this.isLoading = false;
        this.categorizeMeals();
      },
      error: (err) => {
        console.error('Error loading meal plan:', err);
        this.error = 'Failed to load meal plan. Please try again.';
        this.isLoading = false;
      }
    });
  }

  // Categorize meals by type
  categorizeMeals(): void {
    if (!this.mealPlan || !this.mealPlan.items) return;

    this.breakfastItems = this.mealPlan.items.filter((item: any) =>
      item.mealType.toLowerCase() === 'breakfast');

    this.lunchItems = this.mealPlan.items.filter((item: any) =>
      item.mealType.toLowerCase() === 'lunch');

    this.dinnerItems = this.mealPlan.items.filter((item: any) =>
      item.mealType.toLowerCase() === 'dinner');

    this.snackItems = this.mealPlan.items.filter((item: any) =>
      item.mealType.toLowerCase() === 'snack');
  }

  // Get day name from day number
  getDayName(dayOfWeek: number | null): string {
    if (dayOfWeek === null || dayOfWeek === undefined) {
      return 'No specific day';
    }
    return this.weekdays[dayOfWeek];
  }

  // Calculate total calories
  getTotalCalories(): number {
    if (!this.mealPlan || !this.mealPlan.items) return 0;

    return this.mealPlan.items.reduce((sum: number, item: any) => {
      return sum + (parseInt(item.calories) || 0);
    }, 0);
  }

  // Calculate macronutrient totals
  getTotalProtein(): number {
    if (!this.mealPlan || !this.mealPlan.items) return 0;

    return this.mealPlan.items.reduce((sum: number, item: any) => {
      const protein = item.protein.replace('g', '');
      return sum + (parseInt(protein) || 0);
    }, 0);
  }

  getTotalCarbs(): number {
    if (!this.mealPlan || !this.mealPlan.items) return 0;

    return this.mealPlan.items.reduce((sum: number, item: any) => {
      const carbs = item.carbs.replace('g', '');
      return sum + (parseInt(carbs) || 0);
    }, 0);
  }

  getTotalFats(): number {
    if (!this.mealPlan || !this.mealPlan.items) return 0;

    return this.mealPlan.items.reduce((sum: number, item: any) => {
      const fats = item.fats.replace('g', '');
      return sum + (parseInt(fats) || 0);
    }, 0);
  }

  // Edit Meal Plan
  editMealPlan(): void {
    this.router.navigate(['/meal-plans', this.mealPlan.id, 'edit']);
  }

  // Delete meal plan
  deleteMealPlan(): void {
    if (!this.mealPlan) return;

    if (confirm('Are you sure you want to delete this meal plan? This cannot be undone.')) {
      this.isLoading = true;

      this.mealPlanService.deleteMealPlan(this.mealPlan.id).subscribe({
        next: () => {
          this.snackBar.open('Meal plan deleted successfully', 'Close', {
            duration: 3000
          });
          this.router.navigate(['/meal-plans']);
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

  // Go back to meal plans list
  goBack(): void {
    this.router.navigate(['/meal-plans']);
  }


  // Add to google calendar
  addToCalendar(): void {
    this.isLoading = true;

    this.calendarService.addMealPlanToCalendar(this.mealPlan.id)
      .subscribe({
        next: (response) => {
          console.log('Calendar success response:', response);
          this.isLoading = false;
          this.snackBar.open('Meal plan added to your Google Calendar', 'Close', {
            duration: 3000
          });
        },
        error: (err) => {
          console.error('Calendar error response:', err);
          this.isLoading = false;

          if (err.error && err.error.message) {
            console.error('Server error message:', err.error.message);
          }

          // If the user hasn't connected their Google account yet
          if (err.status === 401 || err.status === 403) {
            this.snackBar.open('You need to connect to Google Calendar first', 'Connect', {
              duration: 5000
            }).onAction().subscribe(() => {
              this.calendarService.initiateGoogleAuth();
            });
          } else {
            this.snackBar.open('Failed to add to calendar. Please try again.', 'Close', {
              duration: 3000
            });
          }
        }
      });
  }

  // Prompt Upgrade
  promptUpgrade(): void {
    this.snackBar.open('Google Calendar integration is a premium feature.', 'Upgrade', {
      duration: 5000
    }).onAction().subscribe(() => {
      this.router.navigate(['/upgrade']);
    });
  }

}
