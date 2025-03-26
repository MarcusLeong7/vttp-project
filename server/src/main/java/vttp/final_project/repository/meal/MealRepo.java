package vttp.final_project.repository.meal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.mealModels.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class MealRepo {

    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String, Object> template;

    private static final String USER_MEALS_KEY = "USER:%s:MEALS";  // Format for user's meal collection

    // Save a meal directly to a user's collection
    public void saveMealForUser(String email, Meal meal) {
        // Use hash operation to store meal under user's meals collection
        template.opsForHash().put(String.format(USER_MEALS_KEY, email), meal.getId(), meal);
    }

    // Get all meals for a user
    public List<Meal> getAllUserMeals(String email) {
        // Get all meals from the user's collection
        Map<Object, Object> mealMap = template.opsForHash().entries(String.format(USER_MEALS_KEY, email));

        List<Meal> meals = new ArrayList<>();
        for (Object meal : mealMap.values()) {
            meals.add((Meal) meal);
        }

        return meals;
    }

    // Remove a meal from a user's collection
    public void removeMealFromUser(String email, String mealId) {
        template.opsForHash().delete(String.format(USER_MEALS_KEY, email), mealId);
    }
    // Check if a meal exists in a user's collection
    public boolean userHasMeal(String email, String mealId) {
        return template.opsForHash().hasKey(String.format(USER_MEALS_KEY, email), mealId);
    }

    /*
    // Get a specific meal from a user's collection
    public Meal getUserMeal(String email, String mealId) {
        return (Meal) template.opsForHash().get(String.format(USER_MEALS_KEY, email), mealId);
    }
    */

}

