import {Component, Input, Output} from '@angular/core';
import {Workout} from '../../models/Workout/workout';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-workout-list',
  standalone: false,
  templateUrl: './workout-list.component.html',
  styleUrl: './workout-list.component.css'
})
export class WorkoutListComponent {

  @Input()
  workouts: Workout[] = [];
  @Output()
  selectWorkouts = new Subject<string[]>();
  @Output()
  saveWorkouts = new Subject<Workout[]>();

  // Component properties
  selectedWorkoutIds: string[] = [];

  toggleWorkoutSelection(workoutId: string, event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedWorkoutIds.push(workoutId);
    } else {
      this.selectedWorkoutIds = this.selectedWorkoutIds.filter(id => id !== workoutId);
    }
  }

  saveSelectedWorkouts() {
    if (this.selectedWorkoutIds.length === 0) {
      alert('Please select at least one workout');
      return;
    }

    // Get the complete workout objects for selected IDs
    const selectedWorkouts = this.workouts.filter(workout =>
      this.selectedWorkoutIds.includes(workout.id)
    );

    // Emit the selected workout objects
    this.saveWorkouts.next(selectedWorkouts);
  }
}
