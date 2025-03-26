import {Component, inject, OnInit, ViewEncapsulation} from '@angular/core';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {WeightLogService} from '../../services/weight-log.service';
import {WeightLog} from '../../models/User/user';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  encapsulation: ViewEncapsulation.None // This allows styles to leak out to children
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

  ngOnInit(): void {
    this.weightForm = this.createForm();
    this.loadWeightLogs();
    this.initChartOptions();
  }

  private createForm() {
    return this.fb.group({
      weight: ['', [Validators.required, Validators.min(20), Validators.max(150)]],
      notes: ['']
    })
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

  // Sort weight logs for the table
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

    // Create a proper dataset with enhanced styling
    this.chartData = {
      labels: labels,
      datasets: [
        {
          label: 'Weight (kg)',
          data: weightData,
          fill: true,
          backgroundColor: 'rgba(151, 187, 205, 0.2)',
          borderColor: 'rgba(151, 187, 205, 1)',
          borderWidth: 2,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: 'rgba(151, 187, 205, 1)',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 7,
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(151, 187, 205, 1)',
          pointHoverBorderWidth: 2
        }
      ]
    };
  }

  initChartOptions(): void {
    // Calculate appropriate min and max with buffer
    const minWeight = this.getMinWeight();
    const maxWeight = this.getMaxWeight();
    const range = maxWeight - minWeight;
    const buffer = Math.max(5, range * 0.2);

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          labels: {
            color: '#495057',
            font: {
              weight: 'bold'
            }
          }
        },
        tooltip: {
          mode: 'nearest',
          intersect: false,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          titleColor: '#ffffff',
          bodyColor: '#ffffff',
          borderColor: 'rgba(151, 187, 205, 1)',
          borderWidth: 1,
          padding: 10,
          titleFont: {
            size: 14,
            weight: 'bold'
          },
          bodyFont: {
            size: 13
          },
          displayColors: false,
          callbacks: {
            label: function(context: {raw: number}) {
              return `Weight: ${context.raw} kg`;
            }
          }
        }
      },
      hover: {
        mode: 'nearest',
        intersect: false
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Date',
            color: '#495057',
            font: {
              weight: 'bold'
            }
          },
          ticks: {
            color: '#495057',
            font: {
              weight: 'bold'
            },
            maxRotation: 50
          },
          grid: {
            color: 'rgba(0, 0, 0, 0.1)',
            display: true,
            drawBorder: true
          }
        },
        y: {
          title: {
            display: true,
            text: 'Weight (kg)',
            color: '#495057',
            font: {
              weight: 'bold'
            }
          },
          min: Math.max(0, minWeight - buffer),
          max: maxWeight + buffer,
          ticks: {
            color: '#495057',
            font: {
              weight: 'bold'
            },
            precision: 0,
            stepSize: this.calculateStepSize(minWeight, maxWeight)
          },
          grid: {
            color: 'rgba(0, 0, 0, 0.1)',
            display: true,
            drawBorder: true
          }
        }
      },
      animation: {
        duration: 1000,
        easing: 'easeInOutQuart'
      }
    };
  }

  // Helper methods
  getMinWeight(): number {
    if (this.weightLogs.length === 0) return 50;
    return Math.floor(Math.min(...this.weightLogs.map(log => log.weight)));
  }
  getMaxWeight(): number {
    if (this.weightLogs.length === 0) return 100;
    return Math.ceil(Math.max(...this.weightLogs.map(log => log.weight)));
  }
  calculateStepSize(min: number, max: number): number {
    const range = max - min;
    if (range <= 10) return 1;
    if (range <= 20) return 2;
    if (range <= 40) return 5;
    return 10;
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
