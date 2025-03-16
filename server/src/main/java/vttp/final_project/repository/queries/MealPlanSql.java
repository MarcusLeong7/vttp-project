package vttp.final_project.repository.queries;

public class MealPlanSql {

    // Meal Plan Queries
    public static final String SQL_INSERT_MEAL_PLAN =
            "insert into meal_plans (id, name, description, user_id, day_of_week) values (?, ?, ?, ?, ?)";
    public static final String SQL_GET_MEAL_PLANS_BY_USER =
            "select * from meal_plans where user_id = ? order by created_at desc";
    public static final String SQL_GET_MEAL_PLAN_BY_ID =
            "select * from meal_plans where id = ?";
    public static final String SQL_DELETE_MEAL_PLAN =
            "delete from meal_plans where id = ? and user_id = ?";
    public static final String SQL_UPDATE_MEAL_PLAN =
            "UPDATE meal_plans SET name = ?, description = ?, day_of_week = ? WHERE id = ? AND user_id = ?";
    public static final String SQL_COUNT_MEAL_PLAN="select count(*) from meal_plans where user_id = ?";

    // Meal Plan Item queries
    public static final String SQL_INSERT_MEAL_PLAN_ITEM =
            "insert into meal_plan_items (meal_plan_id, meal_id, meal_title, meal_image, calories, protein, carbs, fats, meal_type) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String SQL_GET_MEAL_PLAN_ITEMS =
            "select * from meal_plan_items where meal_plan_id = ?";
    public static final String SQL_DELETE_MEAL_PLAN_ITEMS =
            "delete from meal_plan_items where meal_plan_id = ?";
}
