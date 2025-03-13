import {Component, inject, OnInit} from '@angular/core';
import {MealService} from '../../services/meal.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Meal, MealSearchParams} from '../../models/Meal/Meal';
import {Router} from '@angular/router';

@Component({
  selector: 'app-meal-search',
  standalone: false,
  templateUrl: './meal-search.component.html',
  styleUrl: './meal-search.component.css'
})
export class MealSearchComponent implements OnInit {

  // Dependency Injections
  private fb = inject(FormBuilder);
  private mealSvc = inject(MealService);
  private router = inject(Router);

  protected searchForm!: FormGroup;

  // Properties/Attributes
  meals: Meal[] = [] // Initialize an empty meals array
  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {

    this.searchForm = this.fb.group({
      maxCalories: [1000],
      minProtein: ['0'],
      maxCarbs: ['100'],
      maxFats: ['100']
    })
  }

  protected search() {
    this.isLoading = true;
    this.errorMessage = '';
    // Extract form search values
    const searchParams: MealSearchParams = this.searchForm.value;

    this.mealSvc.getMeals(searchParams)
      .subscribe({
        next: (meals) => {
          this.meals = meals;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to load meals. Please try again.';
          this.isLoading = false;
          console.error('Error fetching meals:', err);
        }
      });
  }

  resetForm() {
    // Reset the form
    this.searchForm.reset({
      maxCalories: 0,
      minProtein: '0',
      maxCarbs: '0',
      maxFats: '0'
    });
    // Empty meal list
    this.meals = []
  }

  selectMeals(selectedMealIds: string[]) {
    if (selectedMealIds.length === 0) {
      this.errorMessage = 'Please select at least one meal';
      return;
    }
  }

  // Add this method to your MealSearchComponent
  saveMeals(selectedMeals: Meal[]) {
    this.isLoading = true;
    this.errorMessage = '';

    this.mealSvc.saveMeals(selectedMeals)
      .subscribe({
        next: (result) => {
          this.isLoading = false;
          alert('Meals saved successfully');
          // After a short delay, redirect to meals database page
          setTimeout(() => {
            this.router.navigate(['/meals/saved']);
          }, 300);
        },
        error: (err) => {
          this.errorMessage = 'Failed to save meals. Please try again.';
          this.isLoading = false;
          console.error('Error saving meals:', err);
        }
      });
  }
}
