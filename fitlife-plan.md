# FitLife App - Complete Implementation Plan

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Backend Modifications](#backend-modifications)
3. [Database Schema](#database-schema)
4. [Angular Frontend Structure](#angular-frontend-structure)
5. [Component Structure](#component-structure)
6. [API Endpoints](#api-endpoints)
7. [Implementation Roadmap](#implementation-roadmap)
8. [Optional Features](#optional-features)

## Architecture Overview

### System Architecture Diagram
```
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│                     │      │                     │      │                     │
│  Angular Frontend   │◄────►│   Spring Boot API   │◄────►│   Database Layer    │
│                     │      │                     │      │                     │
└─────────────────────┘      └─────────────────────┘      └─────────────────────┘
                                      ▲                           ▲
                                      │                           │
                                      ▼                           │
                             ┌─────────────────────┐             │
                             │                     │             │
                             │  External Services  │─────────────┘
                             │                     │
                             └─────────────────────┘
```

### Technology Stack
- **Frontend**: Angular 17+, Angular Material, Component Store for state management
- **Backend**: Spring Boot, Spring Security, JWT Authentication
- **Primary Database**: MySQL (for structured data)
- **Secondary Database**: Redis (for caching and session management)
- **External Services**: Spoonacular API, Google Calendar API, Firebase Cloud Messaging
- **Deployment**: Docker, Cloud PaaS (Railway or similar)

### Data Flow
1. Angular frontend makes HTTP requests to the Spring Boot REST API
2. API authenticates requests using JWT tokens
3. API processes business logic and interacts with databases
4. API returns JSON responses to the frontend
5. Component Store manages application state on the frontend

## Backend Modifications

### 1. Project Structure Reorganization

```
src/main/java/vttp/ssf/mini_project/
├── config/
│   ├── AppConfig.java (Redis configuration)
│   ├── SecurityConfig.java (JWT security config)
│   └── CorsConfig.java (New file for CORS configuration)
├── controller/ (Thymeleaf controllers - to be removed/refactored)
├── restcontroller/ (Rename to 'api')
│   ├── AuthController.java (New file)
│   ├── MealController.java (Refactored)
│   ├── MealPlanController.java (Refactored)
│   ├── WorkoutController.java (New file)
│   └── UserController.java (New file)
├── entity/ (New package for JPA entities)
│   ├── User.java (JPA entity)
│   ├── UserProfile.java (New file)
│   ├── Meal.java (JPA entity)
│   ├── MealPlan.java (JPA entity)
│   ├── Workout.java (New file)
│   ├── Exercise.java (New file)
│   └── WorkoutSchedule.java (New file)
├── repository/
│   ├── UserRepository.java (Modified for JPA)
│   ├── MealRepository.java (Modified for JPA)
│   ├── MealPlanRepository.java (Modified for JPA)
│   ├── WorkoutRepository.java (New file)
│   ├── ExerciseRepository.java (New file)
│   └── RedisRepository.java (Keeping Redis functionality)
├── service/
│   ├── UserService.java (Modified)
│   ├── MealService.java (Modified)
│   ├── MealPlanService.java (Modified)
│   ├── WorkoutService.java (New file)
│   ├── ExerciseService.java (New file)
│   └── JwtService.java (New file)
├── model/ (DTOs for API requests/responses)
│   ├── LoginRequest.java (New file)
│   ├── RegisterRequest.java (New file)
│   ├── JwtResponse.java (New file)
│   ├── MealDto.java (New file)
│   ├── MealPlanDto.java (New file)
│   ├── WorkoutDto.java (New file)
│   └── ExerciseDto.java (New file)
├── exception/
│   ├── GlobalExceptionHandler.java (New file)
│   ├── ResourceNotFoundException.java (New file)
│   └── UnauthorizedException.java (New file)
└── security/ (New package)
    ├── JwtTokenFilter.java (New file)
    ├── JwtTokenProvider.java (New file)
    └── UserDetailsServiceImpl.java (New file)
```

### 2. Key File Modifications

#### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

#### JwtTokenProvider.java
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
                
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### AuthController.java
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        userService.registerUser(
            registerRequest.getEmail(),
            registerRequest.getPassword()
        );
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
```

#### MealController.java (REST API)
```java
@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;
    
    @GetMapping
    public ResponseEntity<List<MealDto>> getMeals(
            @RequestParam(required = false, defaultValue = "1000") Integer maxCalories,
            @RequestParam(required = false, defaultValue = "0") String minProtein,
            @RequestParam(required = false, defaultValue = "100") String maxCarbs,
            @RequestParam(required = false, defaultValue = "100") String maxFats) {
        
        List<MealDto> meals = mealService.getMeals(maxCalories, minProtein, maxCarbs, maxFats)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(meals);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MealDto> getMealById(@PathVariable String id) {
        Meal meal = mealService.getMeal(id);
        if (meal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToDto(meal));
    }
    
    @GetMapping("/saved")
    public ResponseEntity<List<MealDto>> getSavedMeals(Principal principal) {
        List<MealDto> savedMeals = mealService.getSavedMealsByUser(principal.getName())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(savedMeals);
    }
    
    @PostMapping("/save")
    public ResponseEntity<MealDto> saveMeal(@RequestBody MealDto mealDto, Principal principal) {
        Meal meal = convertToEntity(mealDto);
        Meal savedMeal = mealService.saveMealForUser(meal, principal.getName());
        return ResponseEntity.ok(convertToDto(savedMeal));
    }
    
    // Helper methods for DTO conversion
    private MealDto convertToDto(Meal meal) {
        // Implementation
    }
    
    private Meal convertToEntity(MealDto mealDto) {
        // Implementation
    }
}
```

#### WorkoutController.java (New)
```java
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;
    
    @GetMapping
    public ResponseEntity<List<WorkoutDto>> getWorkouts(Principal principal) {
        List<WorkoutDto> workouts = workoutService.getWorkoutsByUser(principal.getName())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(workouts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutDto> getWorkoutById(@PathVariable String id, Principal principal) {
        Workout workout = workoutService.getWorkoutById(id, principal.getName());
        if (workout == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToDto(workout));
    }
    
    @PostMapping
    public ResponseEntity<WorkoutDto> createWorkout(@Valid @RequestBody WorkoutDto workoutDto, Principal principal) {
        Workout workout = convertToEntity(workoutDto);
        workout.setUserId(principal.getName());
        
        Workout createdWorkout = workoutService.createWorkout(workout);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdWorkout));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutDto> updateWorkout(
            @PathVariable String id, 
            @Valid @RequestBody WorkoutDto workoutDto,
            Principal principal) {
        
        Workout workout = convertToEntity(workoutDto);
        workout.setId(id);
        
        Workout updatedWorkout = workoutService.updateWorkout(workout, principal.getName());
        return ResponseEntity.ok(convertToDto(updatedWorkout));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable String id, Principal principal) {
        workoutService.deleteWorkout(id, principal.getName());
        return ResponseEntity.ok().build();
    }
    
    // Helper methods for DTO conversion
    private WorkoutDto convertToDto(Workout workout) {
        // Implementation
    }
    
    private Workout convertToEntity(WorkoutDto workoutDto) {
        // Implementation
    }
}
```

### 3. Database Configuration

#### application.properties
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/fitlife
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
spring.data.redis.username=
spring.data.redis.password=

# JWT Configuration
jwt.secret=yourSecretKeyHere
jwt.expiration=86400000

# Spoonacular API
spoonacular.api.key=yourApiKeyHere

# Logging
logging.level.org.springframework.web=INFO
logging.level.vttp.ssf.mini_project=DEBUG
```

## Database Schema

### Entity Relationship Diagram
```
┌────────────────┐       ┌────────────────┐       ┌────────────────┐
│     User       │       │  UserProfile   │       │     Meal       │
├────────────────┤       ├────────────────┤       ├────────────────┤
│ id             │─────1─┤ id             │       │ id             │
│ email          │       │ first_name     │       │ title          │
│ password_hash  │       │ last_name      │       │ image          │
│ created_at     │       │ height         │       │ calories       │
└────────────────┘       │ weight         │       │ protein        │
        │                │ fitness_goals   │       │ carbs          │
        │                │ diet_prefs      │       │ fats           │
        │                └────────────────┘       │ source_url      │
        │                                         └────────────────┘
        │                                                 ▲
        │                                                 │
        │                                                 │
        │                ┌────────────────┐      ┌────────────────┐
        │                │   MealPlan     │      │ MealPlanItem   │
        │                ├────────────────┤      ├────────────────┤
        └───────────────►│ id             │─1───►│ id             │
                         │ name           │      │ meal_plan_id   │
                         │ user_id        │      │ meal_id        │◄────
                         │ created_at     │      │ day_of_week    │     │
                         └────────────────┘      │ meal_type      │     │
                                                 └────────────────┘     │
                                                                        │
        │                                                               │
        │                                                               │
        │                ┌────────────────┐                             │
        │                │    Workout     │      ┌────────────────┐     │
        │                ├────────────────┤      │WorkoutExercise │     │
        └───────────────►│ id             │─1───►│ id             │     │
                         │ name           │      │ workout_id     │     │
                         │ description    │      │ exercise_id    │     │
                         │ difficulty     │      │ sets           │     │
                         │ user_id        │      │ reps           │     │
                         │ created_at     │      │ weight         │     │
                         └────────────────┘      │ rest_period    │     │
                                 ▲               └────────────────┘     │
                                 │                       │              │
                                 │                       │              │
                                 │                       ▼              │
        ┌────────────────┐       │               ┌────────────────┐     │
        │WorkoutSchedule │       │               │   Exercise     │     │
        ├────────────────┤       │               ├────────────────┤     │
        │ id             │       │               │ id             │     │
        │ user_id        │───────┘               │ name           │     │
        │ workout_id     │                       │ description    │     │
        │ scheduled_date │                       │ muscle_group   │     │
        │ completed      │                       │ equipment      │     │
        └────────────────┘                       └────────────────┘     │
                                                         ▲              │
                                                         │              │
                                                         └──────────────┘
```

## Angular Frontend Structure

### Project Structure
```
fitlife-frontend/
├── src/
│   ├── app/
│   │   ├── core/                  # Core functionality
│   │   │   ├── auth/              # Authentication
│   │   │   ├── guards/            # Route guards
│   │   │   ├── http/              # HTTP interceptors
│   │   │   ├── services/          # Core services
│   │   │   └── models/            # Shared models/interfaces
│   │   ├── shared/                # Shared components
│   │   │   ├── components/        # Reusable components
│   │   │   ├── directives/        # Custom directives
│   │   │   └── pipes/             # Custom pipes
│   │   ├── features/              # Feature modules
│   │   │   ├── dashboard/         # Dashboard feature
│   │   │   ├── meals/             # Meals feature
│   │   │   ├── workouts/          # Workouts feature
│   │   │   ├── profile/           # User profile feature
│   │   │   └── progress/          # Progress tracking feature
│   │   ├── layout/                # Layout components
│   │   │   ├── header/            # Header component
│   │   │   ├── footer/            # Footer component
│   │   │   ├── sidebar/           # Sidebar component
│   │   │   └── layout.component.ts # Main layout
│   │   ├── store/                 # State management
│   │   │   ├── auth/              # Auth state
│   │   │   ├── meals/             # Meals state
│   │   │   └── workouts/          # Workouts state
│   │   ├── app-routing.module.ts  # Main routing
│   │   ├── app.component.ts       # Root component
│   │   └── app.module.ts          # Root module
│   ├── assets/                    # Static assets
│   ├── environments/              # Environment configurations
│   ├── styles/                    # Global styles
│   ├── index.html                 # Main HTML file
│   └── main.ts                    # Entry point
├── angular.json                   # Angular CLI configuration
├── package.json                   # Dependencies
└── tsconfig.json                  # TypeScript configuration
```

### App Module Configuration
```typescript
@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    // Feature modules
    CoreModule,
    SharedModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

### Main Routing Configuration
```typescript
const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule) 
      },
      { 
        path: 'meals', 
        loadChildren: () => import('./features/meals/meals.module').then(m => m.MealsModule) 
      },
      { 
        path: 'workouts', 
        loadChildren: () => import('./features/workouts/workouts.module').then(m => m.WorkoutsModule) 
      },
      { 
        path: 'profile', 
        loadChildren: () => import('./features/profile/profile.module').then(m => m.ProfileModule) 
      },
      { 
        path: 'progress', 
        loadChildren: () => import('./features/progress/progress.module').then(m => m.ProgressModule) 
      }
    ]
  },
  { 
    path: 'auth', 
    loadChildren: () => import('./core/auth/auth.module').then(m => m.AuthModule) 
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

## Component Structure

### Core Components

#### Layout Component Structure
```typescript
@Component({
  selector: 'app-layout',
  template: `
    <app-header></app-header>
    <div class="main-container">
      <app-sidebar></app-sidebar>
      <div class="content-container">
        <router-outlet></router-outlet>
      </div>
    </div>
    <app-footer></app-footer>
  `,
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {}
```

#### Authentication Components
- **LoginComponent**: User login form
- **RegisterComponent**: User registration form
- **ForgotPasswordComponent**: Password recovery

### Feature Components

#### Dashboard Feature
```typescript
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  providers: [DashboardStore]
})
export class DashboardComponent implements OnInit {
  nutritionSummary$ = this.dashboardStore.nutritionSummary$;
  recentWorkouts$ = this.dashboardStore.recentWorkouts$;
  upcomingWorkouts$ = this.dashboardStore.upcomingWorkouts$;
  
  constructor(private dashboardStore: DashboardStore) {}
  
  ngOnInit(): void {
    this.dashboardStore.loadDashboardData();
  }
}
```

#### Meals Feature
**Components**:
- **MealSearchComponent**: Search and filter meals
- **MealDetailComponent**: View meal details
- **MealPlanListComponent**: List all meal plans
- **MealPlanCreateComponent**: Create a meal plan
- **MealPlanDetailComponent**: View meal plan details
- **NutritionSummaryComponent**: Summary of nutritional information

**Store Example**:
```typescript
@Injectable()
export class MealPlanStore extends ComponentStore<MealPlanState> {
  constructor(private mealService: MealService) {
    super(initialState);
  }
  
  // Selectors
  readonly mealPlans$ = this.select(state => state.mealPlans);
  readonly selectedMealPlan$ = this.select(state => state.selectedMealPlan);
  readonly isLoading$ = this.select(state => state.isLoading);
  
  // Updaters
  readonly setMealPlans = this.updater((state, mealPlans: MealPlan[]) => ({
    ...state,
    mealPlans,
    isLoading: false
  }));
  
  readonly setSelectedMealPlan = this.updater((state, mealPlan: MealPlan) => ({
    ...state,
    selectedMealPlan: mealPlan,
    isLoading: false
  }));
  
  readonly setLoading = this.updater((state, isLoading: boolean) => ({
    ...state,
    isLoading
  }));
  
  // Effects
  readonly loadMealPlans = this.effect(trigger$ => {
    return trigger$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(() => this.mealService.getMealPlans().pipe(
        tap(mealPlans => this.setMealPlans(mealPlans)),
        catchError(error => {
          console.error('Error loading meal plans', error);
          this.setLoading(false);
          return EMPTY;
        })
      ))
    );
  });
  
  readonly loadMealPlan = this.effect((mealPlanId$: Observable<string>) => {
    return mealPlanId$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(id => this.mealService.getMealPlanById(id).pipe(
        tap(mealPlan => this.setSelectedMealPlan(mealPlan)),
        catchError(error => {
          console.error(`Error loading meal plan with id ${id}`, error);
          this.setLoading(false);
          return EMPTY;
        })
      ))
    );
  });
  
  readonly createMealPlan = this.effect((mealPlan$: Observable<MealPlan>) => {
    return mealPlan$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(mealPlan => this.mealService.createMealPlan(mealPlan).pipe(
        tap(createdMealPlan => {
          this.setSelectedMealPlan(createdMealPlan);
          this.loadMealPlans();
        }),
        catchError(error => {
          console.error('Error creating meal plan', error);
          this.setLoading(false);
          return EMPTY;
        })
      ))
    );
  });
}
```

#### Workouts Feature
**Components**:
- **WorkoutListComponent**: List all workouts
- **WorkoutDetailComponent**: View workout details
- **WorkoutCreateComponent**: Create a workout
- **ExerciseLibraryComponent**: Browse exercise library
- **WorkoutScheduleComponent**: Schedule workouts
- **WorkoutTrackerComponent**: Track workout progress

**Component Example**:
```typescript
@Component({
  selector: 'app-workout-create',
  templateUrl: './workout-create.component.html',
  styleUrls: ['./workout-create.component.scss']
})
export class WorkoutCreateComponent implements OnInit {
  workoutForm: FormGroup;
  exercises$: Observable<Exercise[]>;
  isSubmitting = false;
  
  constructor(
    private fb: FormBuilder,
    private workoutService: WorkoutService,
    private exerciseService: ExerciseService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.initForm();
    this.exercises$ = this.exerciseService.getExercises();
  }
  
  initForm(): void {
    this.workoutForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      difficulty: ['intermediate', [Validators.required]],
      exercises: this.fb.array([])
    });
  }
  
  get exerciseControls() {
    return (this.workoutForm.get('exercises') as FormArray).controls;
  }
  
  addExercise(): void {
    const exerciseForm = this.fb.group({
      exerciseId: ['', Validators.required],
      sets: [3, [Validators.required, Validators.min(1)]],
      reps: [10, [Validators.required, Validators.min(1)]],
      weight: [0],
      restPeriod: [60]
    });
    
    (this.workoutForm.get('exercises') as FormArray).push(exerciseForm);
  }
  
  removeExercise(index: number): void {
    (this.workoutForm.get('exercises') as FormArray).removeAt(index);
  }
  
  onSubmit(): void {
    if (this.workoutForm.invalid) {
      return;
    }
    
    this.isSubmitting = true;
    this.workoutService.createWorkout(this.workoutForm.value)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (workout) => {
          this.snackBar.open('Workout created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/workouts', workout.id]);
        },
        error: (error) => {
          this.snackBar.open('Failed to create workout', 'Close', { duration: 3000 });
          console.error('Error creating workout', error);
        }
      });
  }
}
```

#### Profile Feature
**Components**:
- **UserProfileComponent**: View and edit user profile
- **SettingsComponent**: User preferences and settings
- **GoalsComponent**: Set and track fitness goals

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh-token` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### User Endpoints
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/goals` - Get user fitness goals
- `PUT /api/users/goals` - Update user fitness goals

### Meal Endpoints
- `GET /api/meals` - Search/list meals
- `GET /api/meals/{id}` - Get meal details
- `GET /api/meals/saved` - Get user's saved meals
- `POST /api/meals/save` - Save a meal
- `DELETE /api/meals/saved/{id}` - Remove a saved meal

### Meal Plan Endpoints
- `GET /api/mealplans` - Get user's meal plans
- `GET /api/mealplans/{id}` - Get meal plan details
- `POST /api/mealplans` - Create a meal plan
- `PUT /api/mealplans/{id}` - Update a meal plan
- `DELETE /api/mealplans/{id}` - Delete a meal plan

### Workout Endpoints
- `GET /api/workouts` - Get user's workouts
- `GET /api/workouts/{id}` - Get workout details
- `POST /api/workouts` - Create a workout
- `PUT /api/workouts/{id}` - Update a workout
- `DELETE /api/workouts/{id}` - Delete a workout
- `GET /api/workouts/scheduled` - Get scheduled workouts

### Exercise Endpoints
- `GET /api/exercises` - List all exercises
- `GET /api/exercises/{id}` - Get exercise details
- `GET /api/exercises/search` - Search exercises
- `POST /api/exercises` - Create custom exercise
- `PUT /api/exercises/{id}` - Update custom exercise
- `DELETE /api/exercises/{id}` - Delete custom exercise

### Progress Tracking Endpoints
- `GET /api/progress` - Get user's progress data
- `POST /api/progress/weight` - Log weight measurement
- `POST /api/progress/workout` - Log completed workout
- `GET /api/progress/reports` - Get progress reports

## Implementation Roadmap

### Week 1 (Feb 17-23): Foundation Setup
- **Backend**: 
  - Convert Spring Boot controllers to REST API endpoints
  - Set up JWT authentication
  - Configure MySQL database and entities
  - Implement core API endpoints for user management

- **Frontend**:
  - Create Angular project structure
  - Set up Angular Material
  - Implement authentication module
  - Create core layout components

### Week 2 (Feb 24-Mar 1): Core Features - Part 1
- **Backend**:
  - Complete meal planning module
  - Add nutrition tracking endpoints
  - Create database relationships

- **Frontend**:
  - Implement meal search and planning features
  - Create dashboard components
  - Set up Component Store for state management

### Week 3 (Mar 2-8): Core Features - Part 2
- **Backend**:
  - Implement workout API endpoints
  - Create exercise library
  - Add workout scheduling functionality

- **Frontend**:
  - Build workout module components
  - Implement exercise library
  - Create workout planning and scheduling features

### Week 4 (Mar 9-15): Enhanced Features
- **Backend**:
  - Add progress tracking endpoints
  - Implement Google Calendar integration
  - Set up email notifications

- **Frontend**:
  - Create progress tracking components
  - Add data visualization for progress
  - Implement profile management

### Week 5 (Mar 16-22): Refinement & Optional Features
- **Backend**:
  - Add Firebase notification integration
  - Implement caching strategies with Redis
  - Optimize API performance

- **Frontend**:
  - Add push notifications
  - Implement offline capabilities with service workers
  - Add final UI polish and animations

### Week 6 (Mar 23-27): Finalization & Deployment
- **Deployment**:
  - Containerize both frontend and backend with Docker
  - Deploy to cloud service (Railway)
  - Set up CI/CD pipeline with GitHub Actions
- **Testing**:
  - Perform final testing and bug fixes
  - Optimize for performance
  - Prepare presentation materials

## Optional Features

To meet the 25-point requirement, here are the recommended optional features with implementation details:

### 1. Spring Boot Security with JWT (4pts)
**Implementation Details**:
- JWT token-based authentication for API security
- Role-based authorization for admin/user roles
- Token refresh mechanism
- Secure password handling with BCrypt encoding

### 2. UI Component Framework - Angular Material (3pts)
**Implementation Details**:
- Responsive layout using Material's grid system
- Material components for forms, cards, dialogs
- Custom theme with primary/accent colors
- Accessibility support

### 3. Google Calendar Integration (6pts)
**Implementation Details**:
- OAuth2 configuration for Google Calendar API
- Sync workout schedules to user's calendar
- Event creation with workout details
- Notification settings for calendar events

### 4. Email Notifications (3pts)
**Implementation Details**:
- Spring Mail for sending emails
- Weekly meal plan summaries
- Workout reminder emails
- Progress report notifications

### 5. Firebase Web Notifications (10pts)
**Implementation Details**:
- Firebase Cloud Messaging integration
- Service worker for handling notifications
- Custom notification templates
- User preference management for notifications

### 6. Service Worker for Offline Support (4pts)
**Implementation Details**:
- Cache API for offline access
- Offline data synchronization
- Application manifest for installable PWA
- Background sync for pending changes

Total: 30 points (exceeds the 25-point minimum)

### Additional Optional Features (If Needed)
- **Integrate with payment gateway like Stripe (6pts)**: For premium features
- **Use another NoSQL database like MongoDB (6pts)**: For storing workout templates
- **Map integration with Google Maps (4pts)**: For finding nearby gyms or running routes
- **Simple AI for workout recommendations (5pts)**: Based on user goals and progress