package vttp.final_project.services.workoutManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.final_project.models.PaginatedResult;
import vttp.final_project.models.Workout;
import vttp.final_project.repository.workout.WorkoutRepository;

import java.util.List;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepo;

    // Search workouts
    public PaginatedResult<Workout> searchWorkouts(String force, String level, String primaryMuscle, int page, int size) {
        List<Workout> allWorkouts = workoutRepo.searchWorkouts(force, level, primaryMuscle);

        // Calculate pagination values
        int totalItems = allWorkouts.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Ensure page number is valid
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        // Get current page items
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);

        List<Workout> pagedWorkouts;
        if (startIndex < totalItems) {
            pagedWorkouts = allWorkouts.subList(startIndex, endIndex);
        } else {
            pagedWorkouts = List.of(); // Empty list if page is beyond results
        }

        return new PaginatedResult<>(pagedWorkouts, totalItems, totalPages, page);
    }

    // Save a workout
    public void saveWorkout(Workout workout) {
        workoutRepo.saveWorkout(workout);
    }

    // Get all workouts
    public List<Workout> getAllWorkouts() {
        return workoutRepo.getAllWorkouts();
    }

    // Save a workout for a user
    public void saveWorkoutForUser(String email, Workout workout) {
        workoutRepo.saveWorkoutForUser(email, workout);
    }

    // Get all workouts for a user
    public List<Workout> getUserWorkouts(String email) {
        return workoutRepo.getAllUserWorkouts(email);
    }

    // Delete workout for a user
    public boolean deleteWorkoutForUser(String email, String workoutId) {
        // Check if the workout exists for this user
        if (!workoutRepo.userHasWorkout(email, workoutId)) {
            return false;
        }
        // Remove the workout from the user's collection
        workoutRepo.removeWorkoutFromUser(email, workoutId);
        return true;
    }
}
