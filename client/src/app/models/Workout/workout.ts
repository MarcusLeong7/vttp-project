export interface Workout {
  id: string;
  name: string;
  force: string;
  level: string;
  mechanic: string;
  equipment: string;
  primaryMuscles: string[];
  secondaryMuscles: string[];
  instructions: string[];
  category: string;
  images: string[];
}

export interface WorkoutSearchParams {
  force?: string;
  level?: string;
  primaryMuscle?: string;
}
