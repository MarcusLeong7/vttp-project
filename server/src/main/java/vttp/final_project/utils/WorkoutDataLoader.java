package vttp.final_project.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vttp.final_project.models.Workout;
import vttp.final_project.services.WorkoutService;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

@Component
public class WorkoutDataLoader implements CommandLineRunner {

    @Autowired
    private WorkoutService workoutSvc;

    @Value("${workout.file.path}")
    private String workoutDataPath;

    @Override
    public void run(String... args) throws Exception {
        // Load workout data from JSON file
        ObjectMapper mapper = new ObjectMapper();

        File workoutFile = new File(workoutDataPath);

        if (!workoutFile.exists()) {
            System.err.println("Workout data file not found at: " + workoutFile.getAbsolutePath());
            return;
        }
        try (FileInputStream fis = new FileInputStream(workoutFile)) {
            System.out.println("Loading workout data from: " + workoutFile.getAbsolutePath());

            List<Workout> workouts = mapper.readValue(
                    fis,new TypeReference<List<Workout>>() {});

            System.out.println("Loading " + workouts.size() + " workouts into database...");
            // Save each workout to Redis
            for (Workout workout : workouts) {
                workoutSvc.saveWorkout(workout);
            }
            System.out.println("Workout data loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading workout data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
