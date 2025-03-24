import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
  }
)
export class UserProfileService {

  private http = inject(HttpClient);

  // Get user's health data
  getUserHealthData(): Observable<any> {
    return this.http.get<any>('/api/user/health')
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Save user's health data
  saveUserHealthData(healthData: any): Observable<any> {
    return this.http.post<any>('/api/user/health', healthData)
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Delete user's health data
  deleteUserHealthData(): Observable<any> {
    return this.http.delete<any>('/api/user/health')
      .pipe(
        map(response => {
          const data = typeof response === 'string' ? JSON.parse(response) : response;
          return data;
        })
      );
  }

  // Calculate BMI (Body Mass Index)
  calculateBMI(height: number, weight: number): number {
    if (!height || !weight || height <= 0) {
      return 0;
    }

    const heightInMeters = height / 100;
    return Math.round((weight / (heightInMeters * heightInMeters)) * 10) / 10;
  }

  // Get BMI category
  getBMICategory(bmi: number): string {
    if (bmi <= 0) return 'N/A';
    if (bmi < 18.5) return 'Underweight';
    if (bmi < 25) return 'Normal weight';
    if (bmi < 30) return 'Overweight';
    return 'Obese';
  }

  // Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor Equation
  calculateBMR(weight: number, height: number, age: number, gender: string): number {
    if (!weight || !height || !age || !gender) {
      return 0;
    }

    if (gender.toLowerCase() === 'male') {
      // Men: BMR = 10W + 6.25H - 5A + 5
      return Math.round(10 * weight + 6.25 * height - 5 * age + 5);
    } else {
      // Women: BMR = 10W + 6.25H - 5A - 161
      return Math.round(10 * weight + 6.25 * height - 5 * age - 161);
    }
  }

  // Calculate TDEE (Total Daily Energy Expenditure)
  calculateTDEE(bmr: number, activityLevel: string): number {
    if (!bmr || !activityLevel) {
      return 0;
    }

    let activityMultiplier;

    switch (activityLevel.toLowerCase()) {
      case 'sedentary':
        activityMultiplier = 1.2;
        break;
      case 'lightly active':
        activityMultiplier = 1.375;
        break;
      case 'moderately active':
        activityMultiplier = 1.55;
        break;
      case 'very active':
        activityMultiplier = 1.725;
        break;
      case 'extra active':
        activityMultiplier = 1.9;
        break;
      default:
        activityMultiplier = 1.2;
    }

    return Math.round(bmr * activityMultiplier);
  }

}
