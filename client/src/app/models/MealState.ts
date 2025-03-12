// Define the state interface
import {Meal, NutritionInfo} from './Meal';

export interface MealState {
  savedMeals: Meal[];
  selectedMeals: Meal[];
  nutritionSummary: NutritionInfo | null;
  isLoading: boolean;
  error: string | null;
}
