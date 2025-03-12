import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {Meal} from '../../models/Meal';
import {Subject} from 'rxjs';
import {MealService} from '../../services/meal.service';

@Component({
  selector: 'app-meal-list',
  standalone: false,
  templateUrl: './meal-list.component.html',
  styleUrl: './meal-list.component.css'
})
export class MealListComponent {

  private mealSvc = inject(MealService)

  @Input() meals: Meal[] = [];
  @Output() selectMeals = new Subject<string[]>();
  @Output() saveMeals = new Subject<Meal[]>();

  selectedMealIds: string[] = [];

  toggleMealSelection(mealId: string, event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedMealIds.push(mealId);
    } else {
      this.selectedMealIds = this.selectedMealIds.filter(id => id !== mealId);
    }
  }

  viewRecipe(mealId: string) {

    this.mealSvc.getRecipeUrl(mealId).subscribe({
      next: (url) => {
        // Open the external recipe URL in a new tab
        window.open(url, '_blank');
      },
      error: (err) => {
        console.error('Error fetching recipe URL:', err);
        alert('Unable to load the recipe. Please try again later.');
      }
    });
  }

  saveSelectedMeals() {
    if (this.selectedMealIds.length === 0) {
      alert('Please select at least one meal');
      return;
    }

    // Get the complete meal objects for selected IDs
    const selectedMeals = this.meals.filter(meal =>
      this.selectedMealIds.includes(meal.id)
    );

    // Emit the selected meal objects
    this.saveMeals.next(selectedMeals);
  }

}
