package vttp.final_project.controller.userControllers;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.userModels.UserHealthData;
import vttp.final_project.services.userManagement.UserHealthService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user/health")
public class UserHealthController {

    @Autowired
    private UserHealthService userHealthService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserHealthData(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        UserHealthData healthData = userHealthService.getUserHealthData(email);

        if (healthData == null) {
            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("hasHealthData", false)
                    .build();
            return ResponseEntity.ok(response.toString());
        }

        JsonObject resp = Json.createObjectBuilder()
                .add("status", "success")
                .add("hasHealthData", true)
                .add("healthData", healthDataToJson(healthData))
                .build();

        return ResponseEntity.ok(resp.toString());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveUserHealthData(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            String email = principal.getName();
            // Parse data from payload
            UserHealthData healthData = new UserHealthData();

            if (payload.get("height") != null) {
                healthData.setHeight(new BigDecimal(payload.get("height").toString()));
            }
            if (payload.get("weight") != null) {
                healthData.setWeight(new BigDecimal(payload.get("weight").toString()));
            }
            if (payload.get("age") != null) {
                healthData.setAge(Integer.parseInt(payload.get("age").toString()));
            }
            if (payload.get("gender") != null) {
                healthData.setGender(payload.get("gender").toString());
            }
            if (payload.get("activityLevel") != null) {
                healthData.setActivityLevel(payload.get("activityLevel").toString());
            }
            if (payload.get("fitnessGoal") != null) {
                healthData.setFitnessGoal(payload.get("fitnessGoal").toString());
            }
            // Save the health data
            UserHealthData savedData = userHealthService.saveUserHealthData(email, healthData);

            JsonObject resp = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Health data saved successfully")
                    .add("healthData", healthDataToJson(savedData))
                    .build();

            return ResponseEntity.ok(resp.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to save health data: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteUserHealthData(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        boolean deleted = userHealthService.deleteUserHealthData(email);

        if (deleted) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Health data deleted successfully")
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Health data not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
        }
    }

    // Helper method to convert health data to JSON
    private JsonObject healthDataToJson(UserHealthData healthData) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        if (healthData.getHeight() != null) {
            builder.add("height", healthData.getHeight());
        } else {
            builder.addNull("height");
        }

        if (healthData.getWeight() != null) {
            builder.add("weight", healthData.getWeight());
        } else {
            builder.addNull("weight");
        }

        if (healthData.getAge() != null) {
            builder.add("age", healthData.getAge());
        } else {
            builder.addNull("age");
        }

        if (healthData.getGender() != null) {
            builder.add("gender", healthData.getGender());
        } else {
            builder.addNull("gender");
        }

        if (healthData.getActivityLevel() != null) {
            builder.add("activityLevel", healthData.getActivityLevel());
        } else {
            builder.addNull("activityLevel");
        }

        if (healthData.getFitnessGoal() != null) {
            builder.add("fitnessGoal", healthData.getFitnessGoal());
        } else {
            builder.addNull("fitnessGoal");
        }

        if (healthData.getBmi() != null) {
            builder.add("bmi", healthData.getBmi());
        } else {
            builder.addNull("bmi");
        }

        if (healthData.getBmr() != null) {
            builder.add("bmr", healthData.getBmr());
        } else {
            builder.addNull("bmr");
        }

        if (healthData.getTdee() != null) {
            builder.add("tdee", healthData.getTdee());
        } else {
            builder.addNull("tdee");
        }

        return builder.build();
    }

}
