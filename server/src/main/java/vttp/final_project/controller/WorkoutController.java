package vttp.final_project.controller;

import jakarta.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.Workout;
import vttp.final_project.services.WorkoutService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutSvc;

    /* Search Workouts with Optional Filters */
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchWorkouts(
            @RequestParam(required = false) String force,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String primaryMuscle) {

        List<Workout> workouts = workoutSvc.searchWorkouts(force, level, primaryMuscle);

        // Convert list of workouts to JSON array
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Workout workout : workouts) {
            arrayBuilder.add(workoutToJsonObject(workout));
        }

        JsonArray resp = arrayBuilder.build();
        return ResponseEntity.ok(resp.toString());
    }

    /* Save Workouts to Database */
    @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveWorkouts(@RequestBody Map<String, List<Workout>> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        List<Workout> workoutsToSave = payload.get("workouts");
        String email = principal.getName();

        try {
            // Save each workout
            for (Workout workout : workoutsToSave) {
                workoutSvc.saveWorkoutForUser(email, workout);
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Workouts saved successfully")
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to save workouts: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Get all saved workouts for a user */
    @GetMapping(path = "/saved", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSavedWorkouts(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        try {
            List<Workout> savedWorkouts = workoutSvc.getUserWorkouts(email);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Workout workout : savedWorkouts) {
                arrayBuilder.add(workoutToJsonObject(workout));
            }

            JsonArray response = arrayBuilder.build();
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to fetch saved workouts: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Delete workout by id*/
    @DeleteMapping(path = "/saved/{workoutId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteSavedWorkout(@PathVariable String workoutId, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        try {
            boolean deleted = workoutSvc.deleteWorkoutForUser(email, workoutId);

            if (!deleted) {
                JsonObject notFound = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Workout not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound.toString());
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Workout deleted successfully")
                    .build();
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to delete workout: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Helper Method: Constructing a Workout JsonObject */
    private JsonObject workoutToJsonObject(Workout workout) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", workout.getId())
                .add("name", workout.getName())
                .add("force", workout.getForce() != null ? workout.getForce() : "")
                .add("level", workout.getLevel() != null ? workout.getLevel() : "")
                .add("mechanic", workout.getMechanic() != null ? workout.getMechanic() : "")
                .add("equipment", workout.getEquipment() != null ? workout.getEquipment() : "")
                .add("category", workout.getCategory() != null ? workout.getCategory() : "");

        // Add primary muscles
        JsonArrayBuilder primaryMusclesBuilder = Json.createArrayBuilder();
        if (workout.getPrimaryMuscles() != null) {
            for (String muscle : workout.getPrimaryMuscles()) {
                primaryMusclesBuilder.add(muscle);
            }
        }
        builder.add("primaryMuscles", primaryMusclesBuilder);

        // Add secondary muscles
        JsonArrayBuilder secondaryMusclesBuilder = Json.createArrayBuilder();
        if (workout.getSecondaryMuscles() != null) {
            for (String muscle : workout.getSecondaryMuscles()) {
                secondaryMusclesBuilder.add(muscle);
            }
        }
        builder.add("secondaryMuscles", secondaryMusclesBuilder);

        // Add instructions
        JsonArrayBuilder instructionsBuilder = Json.createArrayBuilder();
        if (workout.getInstructions() != null) {
            for (String instruction : workout.getInstructions()) {
                instructionsBuilder.add(instruction);
            }
        }
        builder.add("instructions", instructionsBuilder);

        // Add images
        JsonArrayBuilder imagesBuilder = Json.createArrayBuilder();
        if (workout.getImages() != null) {
            for (String image : workout.getImages()) {
                imagesBuilder.add(image);
            }
        }
        builder.add("images", imagesBuilder);

        return builder.build();
    }
}
