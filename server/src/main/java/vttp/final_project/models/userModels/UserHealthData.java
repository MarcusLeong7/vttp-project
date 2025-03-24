package vttp.final_project.models.userModels;

import java.math.BigDecimal;

public class UserHealthData {

    private Integer id;
    private Integer userId;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer age;
    private String gender;
    private String activityLevel;
    private String fitnessGoal;
    private BigDecimal bmi;
    private Integer bmr;
    private Integer tdee;

    public UserHealthData() {
    }

    public UserHealthData(Integer id, Integer userId, BigDecimal height,
                          BigDecimal weight, Integer age, String gender,
                          String activityLevel, String fitnessGoal,
                          BigDecimal bmi, Integer bmr, Integer tdee) {
        this.id = id;
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.fitnessGoal = fitnessGoal;
        this.bmi = bmi;
        this.bmr = bmr;
        this.tdee = tdee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public Integer getBmr() {
        return bmr;
    }

    public void setBmr(Integer bmr) {
        this.bmr = bmr;
    }

    public Integer getTdee() {
        return tdee;
    }

    public void setTdee(Integer tdee) {
        this.tdee = tdee;
    }

    // Calculate BMI method
    public void calculateBmi() {
        if (height != null && weight != null && height.compareTo(BigDecimal.ZERO) > 0) {
            // BMI = weight(kg) / (height(m) * height(m))
            BigDecimal heightInMeters = height.divide(new BigDecimal(100));
            this.bmi = weight.divide(heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    // Calculate BMR using Mifflin-St Jeor Equation
    public void calculateBmr() {
        if (weight != null && height != null && age != null && gender != null) {
            if (gender.equalsIgnoreCase("male")) {
                // Men: BMR = 10W + 6.25H - 5A + 5
                this.bmr = (int) (10 * weight.doubleValue() + 6.25 * height.doubleValue() - 5 * age + 5);
            } else {
                // Women: BMR = 10W + 6.25H - 5A - 161
                this.bmr = (int) (10 * weight.doubleValue() + 6.25 * height.doubleValue() - 5 * age - 161);
            }
        }
    }

    // Calculate TDEE based on activity level
    public void calculateTdee() {
        if (bmr != null && activityLevel != null) {
            double activityMultiplier;
            switch (activityLevel.toLowerCase()) {
                case "sedentary":
                    activityMultiplier = 1.2;
                    break;
                case "lightly active":
                    activityMultiplier = 1.375;
                    break;
                case "moderately active":
                    activityMultiplier = 1.55;
                    break;
                case "very active":
                    activityMultiplier = 1.725;
                    break;
                case "extra active":
                    activityMultiplier = 1.9;
                    break;
                default:
                    activityMultiplier = 1.2;
            }
            this.tdee = (int) (bmr * activityMultiplier);
        }
    }
}
