import {Component, inject, OnInit} from '@angular/core';
import {WorkoutService} from '../../services/workout.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Workout} from '../../models/Workout/workout';

@Component({
  selector: 'app-saved-workouts',
  standalone: false,
  templateUrl: './saved-workouts.component.html',
  styleUrl: './saved-workouts.component.css'
})
export class SavedWorkoutsComponent implements OnInit {

  // Dependency injections
  private workoutSvc = inject(WorkoutService);
  private snackBar = inject(MatSnackBar);

  // Component properties
  savedWorkouts: Workout[] = [];
  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadSavedWorkouts();
  }

  loadSavedWorkouts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.workoutSvc.getSavedWorkouts()
      .subscribe({
        next: (workouts) => {
          this.savedWorkouts = workouts;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to load saved workouts. Please try again.';
          this.isLoading = false;
          console.error('Error fetching saved workouts:', err);
        }
      });
  }

  deleteWorkout(workoutId: string, event: Event): void {
    // Prevent event bubbling
    event.stopPropagation();

    if (confirm('Are you sure you want to delete this workout?')) {
      this.workoutSvc.deleteSavedWorkout(workoutId)
        .subscribe({
          next: () => {
            this.savedWorkouts = this.savedWorkouts.filter(w => w.id !== workoutId);
            this.snackBar.open('Workout deleted successfully', 'Close', {
              duration: 3000
            });
          },
          error: (err) => {
            this.errorMessage = 'Failed to delete workout. Please try again.';
            console.error('Error deleting workout:', err);
          }
        });
    }
  }

}
