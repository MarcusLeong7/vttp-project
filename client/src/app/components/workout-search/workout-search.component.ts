import {Component, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {WorkoutService} from '../../services/workout.service';
import {Router} from '@angular/router';
import {Workout, WorkoutSearchParams} from '../../models/Workout/workout';
import {PageEvent} from '@angular/material/paginator';

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

  // Pagination properties
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Store the last search params to reuse when changing pages
  lastSearchParams: WorkoutSearchParams | null = null;

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

    // Store the search params for pagination
    this.lastSearchParams = searchParams;
    this.currentPage = 0; // Reset to first page on new search

    this.fetchWorkouts(searchParams, this.currentPage);
  }

  private fetchWorkouts(searchParams: WorkoutSearchParams, page: number) {
    this.workoutSvc.searchWorkouts(searchParams, page, this.pageSize)
      .subscribe({
        next: (data) => {
          this.workouts = data.workouts;
          this.totalItems = data.totalItems;
          this.totalPages = data.totalPages;
          this.currentPage = data.currentPage;
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
    this.workouts = [];
    this.lastSearchParams = null;
    this.totalItems = 0;
    this.totalPages = 0;
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

  // Handle page change events from paginator
  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;

    if (this.lastSearchParams) {
      this.isLoading = true;
      this.fetchWorkouts(this.lastSearchParams, this.currentPage);
    }
  }

}
