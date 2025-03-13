export interface Meal {
  id: string;
  title: string;
  image: string;
  calories: string;
  protein: string;
  carbs: string;
  fats: string;
}

export interface MealSearchParams {
  maxCalories?: number;
  minProtein?: string;
  maxCarbs?: string;
  maxFats?: string;
}

export interface NutritionInfo {
  calories: number;
  protein: number;
  carbs: number;
  fats: number;
}

