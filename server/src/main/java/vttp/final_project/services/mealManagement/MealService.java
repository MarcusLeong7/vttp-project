package vttp.final_project.services.mealManagement;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vttp.final_project.models.mealModels.Meal;
import vttp.final_project.repository.meal.MealRepo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MealService {

    @Autowired
    private MealRepo mealRepo;

    @Value("${my.api.key}")
    private String API_KEY;

    public static final String NUTRIENTS_API_URL = "https://api.spoonacular.com/recipes/findByNutrients";
    public static final String RECIPES_API_URL = "https://api.spoonacular.com/recipes/{id}/information";


    // https://api.spoonacular.com/recipes/findByNutrients
    // ?apiKey=apikey&minCarbs=10&maxCarbs=50&number=2
    public List<Meal> getMeals(int maxCalories, String minProtein, String maxCarbs, String maxFats) {
        //Construct the URL
        String uri = UriComponentsBuilder
                .fromUriString(NUTRIENTS_API_URL)
                .queryParam("apiKey", API_KEY)
                .queryParam("maxCalories", maxCalories)
                .queryParam("minProtein", minProtein)
                .queryParam("maxCarbs", maxCarbs)
                .queryParam("maxFat", maxFats)
                .toUriString();

        // Create the GET request
        RequestEntity<Void> request = RequestEntity
                .get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Use RestTemplate to send the request
        RestTemplate restTemplate = new RestTemplate();
        List<Meal> meals = new ArrayList<>();

        try {
            // Fetch response
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String payload = response.getBody();
            System.out.println(payload);
            // Parse JSON response from a JSON String to Json Object
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray jsonArray = reader.readArray();
            System.out.println(">>> Processing JsonArray");

            // Extract array data
            for (JsonObject jsonObj : jsonArray.getValuesAs(JsonObject.class)) {
                // Access properties of each object
                String id = jsonObj.getJsonNumber("id").toString();
                String title = jsonObj.getString("title");
                String image = jsonObj.getString("image");
                String calories = jsonObj.getJsonNumber("calories").toString();
                String protein = jsonObj.getString("protein");
                String fat = jsonObj.getString("fat");
                String carbs = jsonObj.getString("carbs");

                // Set each meal with relevant data
                Meal meal = new Meal();
                meal.setId(id);
                meal.setTitle(title);
                meal.setImage(image);
                meal.setCalories(calories);
                meal.setProtein(protein);
                meal.setFats(fat);
                meal.setCarbs(carbs);
                // Add to list
                meals.add(meal);
            }
        } catch (Exception ex) {
            System.err.printf("Error fetching data: %s\n", ex.getMessage());
        }

        return meals;
    }

    // https://api.spoonacular.com/recipes/{id}/information
    // ?apiKey=
    public String getRecipesUrl(String id) {

        // Construct the URL
        String url = UriComponentsBuilder.fromUriString(RECIPES_API_URL)
                .queryParam("apiKey", API_KEY)
                .buildAndExpand(id)
                .toUriString();

        // Create the GET request
        RequestEntity<Void> request = RequestEntity
                .get(url)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // RestTemplate to send request
        RestTemplate restTemplate = new RestTemplate();
        String recipeUrl = null;

        try {
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String payload = response.getBody();
            System.out.println(payload);

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject jsonObj = reader.readObject();
            System.out.println(">>> Processing JsonObject");

            // Extract relevant data
            recipeUrl = jsonObj.getString("sourceUrl");
        } catch (Exception ex) {
            System.err.printf("Error fetching data: %s\n", ex.getMessage());
        }

        return recipeUrl;
    }


    /* METHODS FOR MEAL REPO */
    // Save a meal for a user (directly tied to user)
    public void saveMealForUser(String email, Meal meal) {
        mealRepo.saveMealForUser(email, meal);
    }

    // Get all meals for a user
    public List<Meal> getUserMeals(String email) {
        return mealRepo.getAllUserMeals(email);
    }

    // Delete meal for a user
    public boolean deleteMealForUser(String email, String mealId) {
        // Check if the meal exists for this user
        if (!mealRepo.userHasMeal(email, mealId)) {
            return false;
        }
        // Remove the meal from the user's collection
        mealRepo.removeMealFromUser(email, mealId);
        return true;
    }

    /* HELPER METHODS */
    public Map<String, Integer> calculateNutritionTotals(List<Meal> selectedMeals) {
        // Initialize totals
        int totalCalories = 0;
        int totalProtein = 0;
        int totalFats = 0;
        int totalCarbs = 0;

        // Iterate through each meal to calculate totals
        for (Meal meal : selectedMeals) {
            totalCalories += parseIntOrDefault(meal.getCalories());
            totalProtein += parseIntOrDefault(meal.getProtein().replace("g", ""));
            totalFats += parseIntOrDefault(meal.getFats().replace("g", ""));
            totalCarbs += parseIntOrDefault(meal.getCarbs().replace("g", ""));
        }
        // Create a map to store the totals
        Map<String, Integer> totals = new HashMap<>();
        totals.put("totalCalories", totalCalories);
        totals.put("totalProtein", totalProtein);
        totals.put("totalFats", totalFats);
        totals.put("totalCarbs", totalCarbs);

        return totals;
    }

    // Helper method to safely parse integers with a default value
    private int parseIntOrDefault(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if parsing fails
        }
    }


}
