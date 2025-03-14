package vttp.final_project.controller.mealManagement;

import jakarta.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.models.meal.Meal;
import vttp.final_project.services.MealManagement.MealService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    /* Search Meals with Optional Filters */
    /* TESTED */
    @GetMapping(path ="/search",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMeals(
            @RequestParam(required = false, defaultValue = "1000") Integer maxCalories,
            @RequestParam(required = false, defaultValue = "0") String minProtein,
            @RequestParam(required = false, defaultValue = "100") String maxCarbs,
            @RequestParam(required = false, defaultValue = "100") String maxFats) {

        // Get meals from service
        List<Meal> meals = mealService.getMeals(maxCalories, minProtein, maxCarbs, maxFats);
        // Convert list of meals to JSON array
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Meal meal : meals) {
            arrayBuilder.add(mealJsonObj(meal));
        }
        JsonArray resp = arrayBuilder.build();
        return ResponseEntity.ok(resp.toString());
    }

    /* Get Recipe URL for each meal*/
    /* TESTED */
    @GetMapping(path = "/{mealId}/recipe", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRecipeUrl(@PathVariable String mealId) {
        String recipeUrl = mealService.getRecipesUrl(mealId);

        if (recipeUrl == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Recipe not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.toString());
        }

        JsonObject resp = Json.createObjectBuilder()
                .add("status", "success")
                .add("recipeUrl", recipeUrl)
                .build();
        return ResponseEntity.ok(resp.toString());
    }

    /* Save Meals to Redis database */
    /* TESTED */
    @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveMeals(@RequestBody Map<String, List<Meal>> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        List<Meal> mealsToSave = payload.get("meals");
        String email = principal.getName();

        try {
            // Save each meal
            for (Meal meal : mealsToSave) {
                mealService.saveMealForUser(email, meal);
            }
            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Meals saved successfully")
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to save meals: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Get all saved meals for a user */
    @GetMapping(path = "/saved", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSavedMeals(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        try {
            List<Meal> savedMeals = mealService.getUserMeals(email);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Meal meal : savedMeals) {
                arrayBuilder.add(mealJsonObj(meal));
            }
            JsonArray response = arrayBuilder.build();
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to fetch saved meals: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Calculate nutrition summary for selected meals */
    @PostMapping(path = "/nutrition-summary", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> calculateNutritionSummary(@RequestBody Map<String, List<Meal>> payload) {
        List<Meal> selectedMeals = payload.get("meals");

        if (selectedMeals == null || selectedMeals.isEmpty()) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "No meals provided for calculation")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }
        try {
            Map<String, Integer> nutritionTotals = mealService.calculateNutritionTotals(selectedMeals);

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("totalCalories", nutritionTotals.get("totalCalories"))
                    .add("totalProtein", nutritionTotals.get("totalProtein"))
                    .add("totalCarbs", nutritionTotals.get("totalCarbs"))
                    .add("totalFats", nutritionTotals.get("totalFats"))
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to calculate nutrition summary: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Delete meal by id*/
    @DeleteMapping(path = "/saved/{mealId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteSavedMeal(@PathVariable String mealId, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }
        String email = principal.getName();
        try {
            boolean deleted = mealService.deleteMealForUser(email, mealId);

            if (!deleted) {
                JsonObject notFound = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Meal not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound.toString());
            }

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Meal deleted successfully")
                    .build();
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to delete meal: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    /* Helper Method: Constructing a Meal JsonObject */
    private JsonObject mealJsonObj(Meal meal) {
        return Json.createObjectBuilder()
                .add("id", meal.getId())
                .add("title", meal.getTitle())
                .add("image", meal.getImage())
                .add("calories", meal.getCalories())
                .add("protein", meal.getProtein())
                .add("carbs", meal.getCarbs())
                .add("fats", meal.getFats())
                .build();
    }

}
