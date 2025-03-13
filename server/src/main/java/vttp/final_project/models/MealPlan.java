package vttp.final_project.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MealPlan {

    private String id;
    private String name;
    private String description;
    private String userId;
    private Integer dayOfWeek;
    private Timestamp createdAt;
    private List<MealPlanItem> items = new ArrayList<>();

    // Create UUID for MealPlan
    public static String createUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    // Helper method to add a meal to the plan
    public void addItem(MealPlanItem item) {
        items.add(item);
    }

    // Constructor to instantiate UUID for each meal plan
    public MealPlan() {
        this.id = createUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<MealPlanItem> getItems() {
        return items;
    }

    public void setItems(List<MealPlanItem> items) {
        this.items = items;
    }
}
