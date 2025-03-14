import {Component, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {WorkoutService} from '../../services/workout.service';
import {Router} from '@angular/router';
import {Workout, WorkoutSearchParams} from '../../models/Workout/workout';

@Component({
  selector: 'app-workout-search',
  standalone: false,
  templateUrl: './workout-search.component.html',
  styleUrl: './workout-search.component.css'
})
export class WorkoutSearchComponent {

  // Dependency Injections
  private fb = inject(FormBuilder);
  private workoutSvc = inject(WorkoutService);
  private router = inject(Router);

  protected searchForm!: FormGroup;

  // Properties/Attributes
  workouts: Workout[] = [] // Initialize an empty workouts array
  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      force: [''],
      level: [''],
      primaryMuscle: ['']
    });
  }

  protected search() {
    this.isLoading = true;
    this.errorMessage = '';
    // Extract form search values
    const searchParams: WorkoutSearchParams = this.searchForm.value;

    this.workoutSvc.searchWorkouts(searchParams)
      .subscribe({
        next: (workouts) => {
          this.workouts = workouts;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to load workouts. Please try again.';
          this.isLoading = false;
          console.error('Error fetching workouts:', err);
        }
      });
  }

  resetForm() {
    // Reset the form
    this.searchForm.reset({
      force: '',
      level: '',
      primaryMuscle: ''
    });
    // Empty workout list
    this.workouts = []
  }

  selectWorkouts(selectedWorkoutIds: string[]) {
    if (selectedWorkoutIds.length === 0) {
      this.errorMessage = 'Please select at least one workout';
      return;
    }
  }

  saveWorkouts(selectedWorkouts: Workout[]) {
    this.isLoading = true;
    this.errorMessage = '';

    this.workoutSvc.saveWorkouts(selectedWorkouts)
      .subscribe({
        next: (result) => {
          this.isLoading = false;
          alert('Workouts saved successfully');
          // After a short delay, redirect to saved workouts page
          setTimeout(() => {
            this.router.navigate(['/workouts/saved']);
          }, 300);
        },
        error: (err) => {
          this.errorMessage = 'Failed to save workouts. Please try again.';
          this.isLoading = false;
          console.error('Error saving workouts:', err);
        }
      });
  }
}
