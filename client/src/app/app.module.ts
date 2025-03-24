import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {jwtInterceptor} from './interceptors/jwt.interceptor';
import {LoginComponent} from './components/login/login.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {authguard} from './guards/authguard';
import {RegisterComponent} from './components/register/register.component';
import {HeaderComponent} from './components/header/header.component';
import {MealSearchComponent} from './components/meal-search/meal-search.component';
import {MaterialModule} from './angular-material/material.module';
import {MealListComponent} from './components/meal-list/meal-list.component';
import {AuthService} from './services/auth.service';
import {MealService} from './services/meal.service';
import {MealPlanService} from './services/meal.plan.service';
import {SavedMealsComponent} from './components/saved-meals/saved-meals.component';
import {MealStore} from './stores/meal.store';
import { MealPlanDetailComponent } from './components/meal-plan-detail/meal-plan-detail.component';
import { MealPlanListComponent } from './components/meal-plan-list/meal-plan-list.component';
import { MealPlanEditComponent } from './components/meal-plan-edit/meal-plan-edit.component';
import { WorkoutSearchComponent } from './components/workout-search/workout-search.component';
import { WorkoutListComponent } from './components/workout-list/workout-list.component';
import {WorkoutService} from './services/workout.service';
import { SavedWorkoutsComponent } from './components/saved-workouts/saved-workouts.component';
import {CalendarService} from './services/calendar.service';
import { CalendarCallbackComponent } from './components/calendar-callback/calendar-callback.component';
import { ScheduleComponent } from './components/schedule/schedule.component';
import {SafeResourceUrlPipe} from './pipes/safe-resoure-url.pipe';
import {PaymentService} from './services/payment.service';
import { UpgradeComponent } from './components/upgrade/upgrade.component';
import { PaymentSuccessComponent } from './components/payment-success/payment-success.component';

export const appRoutes: Routes = [
  // Auth routes
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  // Home/Dashboard Component
  {path: 'home', component: HomeComponent, canActivate: [authguard]},
  // Features:
  // Meals
  {path: 'meals/search', component: MealSearchComponent, canActivate: [authguard]},
  {path: 'meals/saved', component: SavedMealsComponent, canActivate: [authguard]},
  // Meal Plans
  {path: 'meal-plans', component: MealPlanListComponent, canActivate: [authguard]},
  {path: 'meal-plans/:id', component: MealPlanDetailComponent, canActivate: [authguard]},
  { path: 'meal-plans/:id/edit', component: MealPlanEditComponent, canActivate: [authguard] },
  // Workout
  {path: 'workouts/search', component: WorkoutSearchComponent, canActivate: [authguard]},
  {path: 'workouts/saved', component: SavedWorkoutsComponent, canActivate: [authguard]},
  // Google Calendar
  { path: 'calendar/callback', component: CalendarCallbackComponent },
  { path: 'schedule', component: ScheduleComponent, canActivate: [authguard] },
  // Upgrade Payment Component
  { path: 'upgrade', component: UpgradeComponent, canActivate: [authguard] },
  { path: 'payment/success', component: PaymentSuccessComponent, canActivate: [authguard] },

  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: '**', redirectTo: '/login'}
];

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    RegisterComponent,
    HeaderComponent,
    MealSearchComponent,
    MealListComponent,
    SavedMealsComponent,
    MealPlanListComponent,
    MealPlanDetailComponent,
    MealPlanEditComponent,
    WorkoutSearchComponent,
    WorkoutListComponent,
    SavedWorkoutsComponent,
    CalendarCallbackComponent,
    ScheduleComponent,
    UpgradeComponent,
    PaymentSuccessComponent,
  ],
  imports: [
    BrowserModule, ReactiveFormsModule, RouterModule.forRoot(appRoutes), MaterialModule, SafeResourceUrlPipe,
  ],
  providers: [provideHttpClient(withInterceptors([jwtInterceptor])),
    AuthService, MealService,MealPlanService,WorkoutService,
    CalendarService,PaymentService,MealStore],
  bootstrap: [AppComponent]
})
export class AppModule {
}
