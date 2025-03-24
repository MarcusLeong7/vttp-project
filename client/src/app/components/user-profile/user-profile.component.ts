import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UserProfileService} from '../../services/user-profile.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-user-profile',
  standalone: false,
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {

  // Dependency Injection
  private fb = inject(FormBuilder);
  private profileService = inject(UserProfileService);
  private snackBar = inject(MatSnackBar);

  // Component properties
  healthForm !: FormGroup;
  isLoading = false;
  hasHealthData = false;
  errorMessage = '';

  // Activity level options
  activityLevels = [
    { value: 'sedentary', label: 'Sedentary (little or no exercise)' },
    { value: 'lightly active', label: 'Lightly Active (light exercise 1-3 days/week)' },
    { value: 'moderately active', label: 'Moderately Active (moderate exercise 3-5 days/week)' },
    { value: 'very active', label: 'Very Active (hard exercise 6-7 days/week)' },
    { value: 'extra active', label: 'Extra Active (hard exercise & physical job or 2x training)' }
  ];

  // Fitness goal options
  fitnessGoals = [
    { value: 'weight loss', label: 'Weight Loss' },
    { value: 'maintenance', label: 'Maintenance' },
    { value: 'muscle gain', label: 'Muscle Gain' },
    { value: 'athletic performance', label: 'Athletic Performance' },
    { value: 'general health', label: 'General Health' }
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadUserHealthData();
  }

  initForm(): void {
    this.healthForm = this.fb.group({
      height: [null, [Validators.min(50), Validators.max(300)]],
      weight: [null, [Validators.min(20), Validators.max(500)]],
      age: [null, [Validators.min(13), Validators.max(120)]],
      gender: ['', Validators.required],
      activityLevel: ['', Validators.required],
      fitnessGoal: ['', Validators.required]
    });
  }

  loadUserHealthData(): void {
    this.isLoading = true;

    this.profileService.getUserHealthData().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.hasHealthData = data.hasHealthData;

        if (data.hasHealthData && data.healthData) {
          this.healthForm.patchValue({
            height: data.healthData.height,
            weight: data.healthData.weight,
            age: data.healthData.age,
            gender: data.healthData.gender,
            activityLevel: data.healthData.activityLevel,
            fitnessGoal: data.healthData.fitnessGoal
          });
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load health data. Please try again.';
        console.error('Error loading health data:', err);
      }
    });
  }

  saveHealthData(): void {
    if (this.healthForm.invalid) {
      this.errorMessage = 'Please correct the form errors before submitting.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const healthData = this.healthForm.value;

    this.profileService.saveUserHealthData(healthData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.hasHealthData = true;
        this.snackBar.open('Health data saved successfully', 'Close', {
          duration: 3000
        });

        // Update form with calculated values
        if (response.healthData) {
          this.healthForm.patchValue({
            bmi: response.healthData.bmi,
            bmr: response.healthData.bmr,
            tdee: response.healthData.tdee
          });
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to save health data. Please try again.';
        console.error('Error saving health data:', err);
      }
    });
  }

  resetForm(): void {
    this.healthForm.reset();
    this.errorMessage = '';
  }

  calculateBMI(): number {
    const height = this.healthForm.get('height')?.value;
    const weight = this.healthForm.get('weight')?.value;

    if (height && weight && height > 0) {
      const heightInMeters = height / 100;
      return Math.round((weight / (heightInMeters * heightInMeters)) * 10) / 10;
    }

    return 0;
  }

  getBmiCategory(bmi: number): string {
    if (bmi <= 0) return 'N/A';
    if (bmi < 18.5) return 'Underweight';
    if (bmi < 25) return 'Normal weight';
    if (bmi < 30) return 'Overweight';
    return 'Obese';
  }

}
