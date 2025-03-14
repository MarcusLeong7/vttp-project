package vttp.final_project.models.mealModels;

public class Meal {

    private String id;
    private String title;
    private String image;
    private String calories;
    private String protein;
    private String carbs;
    private String fats;

    public Meal() {
    }

    public Meal(String id, String title, String image, String calories, String protein, String carbs, String fats) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public String getCarbs() {
        return carbs;
    }

    public void setCarbs(String carbs) {
        this.carbs = carbs;
    }

    public String getFats() {
        return fats;
    }

    public void setFats(String fats) {
        this.fats = fats;
    }

    @Override
    public String toString() {
        return "Meal{" +
               "title='" + title + '\'' +
               ", imageurl='" + image + '\'' +
               ", calories=" + calories +
               ", protein='" + protein + '\'' +
               ", carbs='" + carbs + '\'' +
               ", fats='" + fats + '\'' +
               '}';
    }
}
