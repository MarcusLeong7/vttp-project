package vttp.final_project.services.workoutManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.final_project.models.Workout;
import vttp.final_project.repository.workout.WorkoutRepository;

import java.util.List;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepo;

    // Search workouts
    public List<Workout> searchWorkouts(String force, String level, String primaryMuscle) {
        return workoutRepo.searchWorkouts(force, level, primaryMuscle);
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
