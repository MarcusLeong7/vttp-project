package vttp.final_project.controller;

import jakarta.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.MealPlan;
import vttp.final_project.models.MealPlanItem;
import vttp.final_project.services.MealPlanService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/mealplans")
public class MealPlanController {

    @Autowired
    private MealPlanService mealPlanSvc;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMealPlans(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String email = principal.getName();
        List<MealPlan> mealPlans = mealPlanSvc.getMealPlansByUser(email);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (MealPlan mealPlan : mealPlans) {
            arrayBuilder.add(mealPlanToJsonObject(mealPlan));
        }

        JsonArray response = arrayBuilder.build();
        return ResponseEntity.ok(response.toString());
    }

    /* DELETE SPECIFIC MEAL PLAN */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteMealPlan(@PathVariable String id, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String userId = principal.getName();
        boolean deleted = mealPlanSvc.deleteMealPlan(id, userId);
        if (!deleted) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Meal plan not found or you don't have permission to delete it")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
        }
        JsonObject response = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Meal plan deleted successfully")
                .build();
        return ResponseEntity.ok(response.toString());
    }


    // Helper method to convert MealPlan to JsonObject
    private JsonObject mealPlanToJsonObject(MealPlan mealPlan) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", mealPlan.getId())
                .add("name", mealPlan.getName())
                // If description is null, use an empty string instead
                .add("description", mealPlan.getDescription() != null ? mealPlan.getDescription() : "")
                .add("userId", mealPlan.getUserId());

        // For Integer values, handle specific null values
        // Add day of week if it exists
        if (mealPlan.getDayOfWeek() != null) {
            builder.add("dayOfWeek", mealPlan.getDayOfWeek());
        } else {
            builder.addNull("dayOfWeek");
        }
        builder.add("createdAt", mealPlan.getCreatedAt() != null ?
                mealPlan.getCreatedAt().toString() : "");

        // For List< MealPlanItem>
        JsonArrayBuilder itemsBuilder = Json.createArrayBuilder();
        for (MealPlanItem item : mealPlan.getItems()) {
            JsonObjectBuilder itemBuilder = Json.createObjectBuilder()
                    .add("id", item.getId())
                    .add("mealId", item.getMealId())
                    .add("mealTitle", item.getMealTitle())
                    .add("mealImage", item.getMealImage())
                    .add("calories", item.getCalories())
                    .add("protein", item.getProtein())
                    .add("carbs", item.getCarbs())
                    .add("fats", item.getFats());

            if (item.getMealType() != null) {
                itemBuilder.add("mealType", item.getMealType());
            } else {
                itemBuilder.addNull("mealType");
            }
            itemsBuilder.add(itemBuilder);
        }
        builder.add("items", itemsBuilder);

        return builder.build();
    }
}
