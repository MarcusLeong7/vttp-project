package vttp.final_project.services.mealManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.final_project.models.mealModels.Meal;
import vttp.final_project.models.mealModels.MealPlan;
import vttp.final_project.models.mealModels.MealPlanItem;
import vttp.final_project.models.userModels.User;
import vttp.final_project.repository.meal.MealPlanRepo;
import vttp.final_project.repository.user.UserSqlRepository;

import java.util.List;

@Service
public class MealPlanService {

    @Autowired
    private MealPlanRepo mealPlanRepo;

    @Autowired
    private UserSqlRepository userSqlRepo;

    /* Create new meal plan */
    public MealPlan createMealPlan(MealPlan mealPlan, String email) {
        mealPlan.setUserId(email);
        return mealPlanRepo.saveMealPlan(mealPlan);
    }

    /* Get all meal plans by User */
    public List<MealPlan> getMealPlansByUser(String email) {
        return mealPlanRepo.getMealPlansByUser(email);
    }

    /* Get a specific meal plan by ID */
    public MealPlan getMealPlanById(String id) {
        return mealPlanRepo.getMealPlanById(id);
    }

    /* Update specific meal plan */
    public boolean updateMealPlan(MealPlan mealPlan, String email) {
        // Ensure the meal plan belongs to this user
        mealPlan.setUserId(email);
        return mealPlanRepo.updateMealPlan(mealPlan);
    }

    /* Delete a meal plan*/
    public boolean deleteMealPlan(String id, String email) {
        return mealPlanRepo.deleteMealPlan(id, email);
    }

    /* For premium feature*/
    public boolean canCreateMoreMealPlans(String email) {
        User user = userSqlRepo.findByEmail(email);

        if (user.isPremium()) {
            return true; // Premium users can create unlimited meal plans
        }

        // Free users are limited to 3 meal plans
        int mealPlanCount = mealPlanRepo.countMealPlansByUser(email);
        return mealPlanCount < 3;
    }

    /* Helper method to convert a Meal to MealPlanItem Object*/
    public MealPlanItem createMealItem (Meal meal, int dayOfWeek, String mealType) {
        MealPlanItem item = new MealPlanItem();
        item.setMealId(meal.getId());
        item.setMealTitle(meal.getTitle());
        item.setMealImage(meal.getImage());
        item.setCalories(meal.getCalories());
        item.setProtein(meal.getProtein());
        item.setCarbs(meal.getCarbs());
        item.setFats(meal.getFats());
        item.setMealType(mealType);
        return item;
    }

}
