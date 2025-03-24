import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {WeightLogService} from '../../services/weight-log.service';
import {WeightLog} from '../../models/User/user';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  private router = inject(Router);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private weightLogService = inject(WeightLogService);

  weightLogs: WeightLog[] = [];
  isLoading = false;
  errorMessage = '';
  weightForm!: FormGroup;

  // Chart data
  chartData: any;
  chartOptions: any;

  constructor() {
    this.weightForm = this.fb.group({
      weight: ['', [Validators.required, Validators.min(20), Validators.max(200)]],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadWeightLogs();
    this.initChartOptions();
  }

  loadWeightLogs(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.weightLogService.getWeightLogs(30).subscribe({
      next: (logs) => {
        this.weightLogs = logs;
        this.prepareChartData();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load weight logs';
        this.isLoading = false;
        console.error('Error loading weight logs:', err);
      }
    });
  }

  prepareChartData(): void {
    if (this.weightLogs.length === 0) {
      return;
    }

    // Sort logs by date (oldest to newest)
    const sortedLogs = [...this.weightLogs].sort((a, b) =>
      new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    const labels = sortedLogs.map(log => {
      const date = new Date(log.date);
      return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
    });

    const weightData = sortedLogs.map(log => log.weight);

    this.chartData = {
      labels: labels,
      datasets: [
        {
          label: 'Weight (kg)',
          data: weightData,
          fill: false,
          borderColor: '#4bc0c0',
          tension: 0.4,
          pointBackgroundColor: '#4bc0c0',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: '#4bc0c0'
        }
      ]
    };
  }

  initChartOptions(): void {
    this.chartOptions = {
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Date'
          },
          ticks: {
            color: '#495057'
          },
          grid: {
            color: '#ebedef'
          }
        },
        y: {
          title: {
            display: true,
            text: 'Weight (kg)'
          },
          ticks: {
            color: '#495057'
          },
          grid: {
            color: '#ebedef'
          }
        }
      }
    };
  }

  logWeight(): void {
    if (this.weightForm.invalid) {
      return;
    }

    const {weight, notes} = this.weightForm.value;
    this.isLoading = true;

    this.weightLogService.addWeightLog(weight, notes).subscribe({
      next: (log) => {
        this.isLoading = false;
        this.snackBar.open('Weight logged successfully', 'Close', {
          duration: 3000
        });
        this.weightForm.reset();
        this.loadWeightLogs(); // Reload logs
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to log weight';
        console.error('Error logging weight:', err);
      }
    });
  }

  // Sorted weight logs for the table
  get sortedWeightLogs(): WeightLog[] {
    return [...this.weightLogs].sort((a, b) =>
      new Date(b.date).getTime() - new Date(a.date).getTime()
    );
  }

// Delete a weight log
  deleteWeightLog(id: number, event: Event): void {
    event.stopPropagation(); // Prevent event bubbling

    if (confirm('Are you sure you want to delete this weight log?')) {
      this.isLoading = true;

      this.weightLogService.deleteWeightLog(id).subscribe({
        next: () => {
          this.snackBar.open('Weight log deleted successfully', 'Close', {
            duration: 3000
          });
          this.loadWeightLogs(); // Reload logs
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete weight log';
          console.error('Error deleting weight log:', err);
        }
      });
    }
  }

  // Navigation methods for the three main features
  navigateToMealSearch(): void {
    this.router.navigate(['/meals/search']);
  }

  navigateToWorkoutSearch(): void {
    this.router.navigate(['/workouts/search']);
  }

  navigateToCalendar(): void {
    this.router.navigate(['/schedule']);
  }

}
