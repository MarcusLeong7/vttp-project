import {Component, inject} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  private router = inject(Router);

  // Navigation methods for the three main features
  navigateToMealSearch(): void {
    this.router.navigate(['/meals/search']);
  }

  navigateToWorkoutSearch(): void {
    this.router.navigate(['/workouts/search']);
  }

  navigateToCalendar(): void {
    this.router.navigate(['/schedule/calendar']);
  }

}
