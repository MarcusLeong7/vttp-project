package vttp.final_project.repository.workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.Workout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class WorkoutRepository {

    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String, Object> template;

    private static final String WORKOUT_KEY = "WORKOUT";
    private static final String USER_WORKOUTS_KEY = "USER:%s:WORKOUTS";
    private static final String WORKOUT_BY_FORCE = "WORKOUT:FORCE:%s";
    private static final String WORKOUT_BY_LEVEL = "WORKOUT:LEVEL:%s";
    private static final String WORKOUT_BY_MUSCLE = "WORKOUT:MUSCLE:%s";

    // Save a workout to the global collection and indices
    public void saveWorkout(Workout workout) {
        // Save to main workout collection
        template.opsForHash().put(WORKOUT_KEY, workout.getId(), workout);

        // Add to force index
        if (workout.getForce() != null && !workout.getForce().isEmpty()) {
            template.opsForSet().add(String.format(WORKOUT_BY_FORCE, workout.getForce()), workout.getId());
        }

        // Add to level index
        if (workout.getLevel() != null && !workout.getLevel().isEmpty()) {
            template.opsForSet().add(String.format(WORKOUT_BY_LEVEL, workout.getLevel()), workout.getId());
        }

        // Add to primary muscle indices
        if (workout.getPrimaryMuscles() != null) {
            for (String muscle : workout.getPrimaryMuscles()) {
                template.opsForSet().add(String.format(WORKOUT_BY_MUSCLE, muscle), workout.getId());
            }
        }
    }

    // Get a specific workout by ID
    public Workout getWorkout(String id) {
        return (Workout) template.opsForHash().get(WORKOUT_KEY, id);
    }

    // Get all workouts
    public List<Workout> getAllWorkouts() {
        Map<Object, Object> workoutMap = template.opsForHash().entries(WORKOUT_KEY);
        List<Workout> workouts = new ArrayList<>();

        for (Object workout : workoutMap.values()) {
            workouts.add((Workout) workout);
        }

        return workouts;
    }

    // Search workouts by criteria
    public List<Workout> searchWorkouts(String force, String level, String primaryMuscle) {
        List<String> workoutIds = new ArrayList<>();
        boolean hasSearchCriteria = false;

        // Search by force
        if (force != null && !force.isEmpty()) {
            workoutIds = new ArrayList<>(template.opsForSet().members(String.format(WORKOUT_BY_FORCE, force)))
                    .stream().map(Object::toString).collect(Collectors.toList());
            hasSearchCriteria = true;
        }

        // Search by level
        if (level != null && !level.isEmpty()) {
            List<String> levelWorkoutIds = new ArrayList<>(template.opsForSet().members(String.format(WORKOUT_BY_LEVEL, level)))
                    .stream().map(Object::toString).collect(Collectors.toList());

            if (hasSearchCriteria) {
                // Intersection with previous results
                workoutIds.retainAll(levelWorkoutIds);
            } else {
                workoutIds = levelWorkoutIds;
                hasSearchCriteria = true;
            }
        }

        // Search by primary muscle
        if (primaryMuscle != null && !primaryMuscle.isEmpty()) {
            List<String> muscleWorkoutIds = new ArrayList<>(template.opsForSet().members(String.format(WORKOUT_BY_MUSCLE, primaryMuscle)))
                    .stream().map(Object::toString).collect(Collectors.toList());

            if (hasSearchCriteria) {
                // Intersection with previous results
                workoutIds.retainAll(muscleWorkoutIds);
            } else {
                workoutIds = muscleWorkoutIds;
                hasSearchCriteria = true;
            }
        }

        // If no criteria specified, return all workouts
        if (!hasSearchCriteria) {
            return getAllWorkouts();
        }

        // Fetch full workout objects
        List<Workout> workouts = new ArrayList<>();
        for (String id : workoutIds) {
            Workout workout = getWorkout(id);
            if (workout != null) {
                workouts.add(workout);
            }
        }

        return workouts;
    }

    // Save a workout for a specific user
    public void saveWorkoutForUser(String email, Workout workout) {
        template.opsForHash().put(String.format(USER_WORKOUTS_KEY, email), workout.getId(), workout);
    }

    // Get all workouts for a specific user
    public List<Workout> getAllUserWorkouts(String email) {
        Map<Object, Object> workoutMap = template.opsForHash().entries(String.format(USER_WORKOUTS_KEY, email));
        List<Workout> workouts = new ArrayList<>();

        for (Object workout : workoutMap.values()) {
            workouts.add((Workout) workout);
        }

        return workouts;
    }

    // Delete a workout from a user's collection
    public void removeWorkoutFromUser(String email, String workoutId) {
        template.opsForHash().delete(String.format(USER_WORKOUTS_KEY, email), workoutId);
    }

    // Check if a workout exists in a user's collection
    public boolean userHasWorkout(String email, String workoutId) {
        return template.opsForHash().hasKey(String.format(USER_WORKOUTS_KEY, email), workoutId);
    }

}
