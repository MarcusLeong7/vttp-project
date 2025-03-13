import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MealPlanService} from '../../services/meal.plan.service';
import { MealType } from '../../models/MealPlan/MealPlan';

@Component({
  selector: 'app-meal-plan-edit',
  standalone: false,
  templateUrl: './meal-plan-edit.component.html',
  styleUrl: './meal-plan-edit.component.css'
})
export class MealPlanEditComponent implements OnInit {

  // Dependency Injections
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private mealPlanService = inject(MealPlanService);

  // Component properties
  mealPlan: any = null;
  isLoading = false;
  error: string | null = null;

  // Form
  editForm !: FormGroup;

  // Map to store meal type assignments
  mealTypeAssignments = new Map<string, MealType>();

  // Day options for dropdown
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

  constructor() {
    this.editForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      dayOfWeek: [null]
    });
  }

  ngOnInit(): void {
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
        this.initializeForm();

        // Initialize meal type assignments
        this.mealPlan.items.forEach((item: any) => {
          this.mealTypeAssignments.set(item.mealId, item.mealType);
        });
      },
      error: (err) => {
        console.error('Error loading meal plan:', err);
        this.error = 'Failed to load meal plan. Please try again.';
        this.isLoading = false;
      }
    });
  }

  // Initialize form with meal plan data
  initializeForm(): void {
    this.editForm.patchValue({
      name: this.mealPlan.name,
      description: this.mealPlan.description || '',
      dayOfWeek: this.mealPlan.dayOfWeek
    });
  }

  // Update meal type assignment
  updateMealType(mealId: string, type: MealType): void {
    this.mealTypeAssignments.set(mealId, type);
  }

  // Get meal type for a meal
  getMealType(mealId: string): MealType {
    return this.mealTypeAssignments.get(mealId) || MealType.BREAKFAST;
  }

  // Save updated meal plan
  saveChanges(): void {
    if (this.editForm.invalid) {
      return;
    }

    this.isLoading = true;

    // Prepare updated meal items with their types
    const mealItems = this.mealPlan.items.map((item: any) => ({
      mealId: item.mealId,
      mealTitle: item.mealTitle,
      mealImage: item.mealImage,
      calories: item.calories,
      protein: item.protein,
      carbs: item.carbs,
      fats: item.fats,
      mealType: this.getMealType(item.mealId) // Use assigned meal type
    }));

    // Prepare updated meal plan data
    const updatedMealPlan = {
      ...this.editForm.value,
      meals: mealItems
    };

    this.mealPlanService.updateMealPlan(this.mealPlan.id, updatedMealPlan).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open('Meal plan updated successfully', 'Close', {
          duration: 3000
        });
        // Navigate back to the detail view
        this.router.navigate(['/meal-plans', this.mealPlan.id]);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error updating meal plan:', err);
        this.error = 'Failed to update meal plan. Please try again.';
        this.snackBar.open('Failed to update meal plan', 'Close', {
          duration: 3000
        });
      }
    });
  }

  // Cancel editing and go back to detail view
  cancel(): void {
    this.router.navigate(['/meal-plans', this.mealPlan.id]);
  }

}
