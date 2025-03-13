import { Meal } from "../Meal/Meal";


// For creating a new meal plan (input to API)
export interface MealPlanRequest {
  name: string;
  description: string;
  dayOfWeek?: number;
  meals: Meal[];  // Regular meals that need to be converted to MealPlanItems
}

// For the full meal plan (response from API)
export interface MealPlan {
  id: string;
  name: string;
  description: string;
  userId: string;
  dayOfWeek?: number;
  createdAt?: string;
  items: MealPlanItem[];  // Contains fully processed meal items
}

// For individual items in a meal plan
// Naming must map to backend
export interface MealPlanItem {
  id?: number;
  mealId: string;
  mealTitle: string;
  mealImage: string;
  calories: string;
  protein: string;
  carbs: string;
  fats: string;
  mealType: MealType;
}

export enum MealType {
  BREAKFAST = 'breakfast',
  LUNCH = 'lunch',
  DINNER = 'dinner',
  SNACK = 'snack'
}



