package vttp.final_project.repository.meal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vttp.final_project.models.mealModels.MealPlan;
import vttp.final_project.models.mealModels.MealPlanItem;

import java.util.ArrayList;
import java.util.List;

import static vttp.final_project.repository.queries.MealPlanSql.*;

@Repository
public class MealPlanRepo {

    @Autowired
    private JdbcTemplate template;

    /* SAVE MEAL PLAN */
    @Transactional
    public MealPlan saveMealPlan(MealPlan mealPlan) {
        // Insert meal plan
        template.update(SQL_INSERT_MEAL_PLAN, mealPlan.getId(), mealPlan.getName(),
                mealPlan.getDescription(), mealPlan.getUserId(),mealPlan.getDayOfWeek());

        // Insert all meal plan items into meal plan
        for (MealPlanItem item : mealPlan.getItems()) {
            item.setMealPlanId(mealPlan.getId());
            template.update(
                 SQL_INSERT_MEAL_PLAN_ITEM, item.getMealPlanId(), item.getMealId(),
                    item.getMealTitle(), item.getMealImage(), item.getCalories(), item.getProtein(), item.getCarbs(),
                    item.getFats(), item.getMealType()
            );
        }
        return mealPlan;
    }

    /* GET LIST OF MEAL PLANS BY USER */
    public List<MealPlan> getMealPlansByUser(String email) {
        List<MealPlan> mealPlans = new ArrayList<>();

        SqlRowSet rs = template.queryForRowSet(SQL_GET_MEAL_PLANS_BY_USER, email);
        while (rs.next()) {
            MealPlan mealPlan = getMealPlan(rs);
            // Get all items for this meal plan
            SqlRowSet itemsRs = template.queryForRowSet(SQL_GET_MEAL_PLAN_ITEMS, mealPlan.getId());
            while (itemsRs.next()) {
                MealPlanItem item = getMealPlanItem(itemsRs);
                mealPlan.addItem(item);
            }
            mealPlans.add(mealPlan);
        }
        return mealPlans;
    }

    /* GET A SINGLE MEAL PLAN */
    public MealPlan getMealPlanById(String id) {
        SqlRowSet rs = template.queryForRowSet(SQL_GET_MEAL_PLAN_BY_ID, id);
        if (rs.next()) {
            MealPlan mealPlan = getMealPlan(rs);
            // Get all items for this meal plan
            SqlRowSet itemsRs = template.queryForRowSet(SQL_GET_MEAL_PLAN_ITEMS, mealPlan.getId());
            while (itemsRs.next()) {
                MealPlanItem item = getMealPlanItem(itemsRs);
                mealPlan.addItem(item);
            }
            return mealPlan;
        }
        return null;
    }

    /* Update individual Meal Plan*/
    @Transactional
    public boolean updateMealPlan(MealPlan mealPlan) {
        // First update the meal plan basic information
        int rowsUpdated = template.update(
                SQL_UPDATE_MEAL_PLAN,
                mealPlan.getName(),
                mealPlan.getDescription(),
                mealPlan.getDayOfWeek(),
                mealPlan.getId(),
                mealPlan.getUserId()
        );

        if (rowsUpdated > 0) {
            // Then delete all existing meal plan items
            template.update(SQL_DELETE_MEAL_PLAN_ITEMS, mealPlan.getId());

            // Then insert all the new meal plan items
            for (MealPlanItem item : mealPlan.getItems()) {
                item.setMealPlanId(mealPlan.getId());
                template.update(
                        SQL_INSERT_MEAL_PLAN_ITEM,
                        item.getMealPlanId(),
                        item.getMealId(),
                        item.getMealTitle(),
                        item.getMealImage(),
                        item.getCalories(),
                        item.getProtein(),
                        item.getCarbs(),
                        item.getFats(),
                        item.getMealType()
                );
            }

            return true;
        }

        return false;
    }

    /* DELETE A MEAL PLAN */
    @Transactional
    public boolean deleteMealPlan(String id, String email) {
        // Delete Meal Plan Items
        template.update(SQL_DELETE_MEAL_PLAN_ITEMS, id);
        // Delete Meal Plan
        int rowDeleted = template.update(SQL_DELETE_MEAL_PLAN, id, email);
        return rowDeleted > 0;
    }

    /* HELPER METHODS */
    // Helper method to map from SQL row to MealPlan object
    private MealPlan getMealPlan(SqlRowSet rs) {
        MealPlan mealPlan = new MealPlan();
        mealPlan.setId(rs.getString("id"));
        mealPlan.setName(rs.getString("name"));
        mealPlan.setDescription(rs.getString("description"));
        mealPlan.setUserId(rs.getString("user_id"));
        mealPlan.setDayOfWeek(rs.getInt("day_of_week"));
        mealPlan.setCreatedAt(rs.getTimestamp("created_at"));
        return mealPlan;
    }
    // Helper method to map from SQL row to MealPlanItem object
    private MealPlanItem getMealPlanItem(SqlRowSet rs) {
        MealPlanItem item = new MealPlanItem();
        item.setId(rs.getInt("id"));
        item.setMealPlanId(rs.getString("meal_plan_id"));
        item.setMealId(rs.getString("meal_id"));
        item.setMealTitle(rs.getString("meal_title"));
        item.setMealImage(rs.getString("meal_image"));
        item.setCalories(rs.getString("calories"));
        item.setProtein(rs.getString("protein"));
        item.setCarbs(rs.getString("carbs"));
        item.setFats(rs.getString("fats"));
        item.setMealType(rs.getString("meal_type"));
        return item;
    }
}

