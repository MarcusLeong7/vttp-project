package vttp.final_project.models;

import java.util.List;

public class Workout {

    private String id;
    private String name;
    private String force;
    private String level;
    private String mechanic;
    private String equipment;
    private List<String> primaryMuscles;
    private List<String> secondaryMuscles;
    private List<String> instructions;
    private String category;
    private List<String> images;


    public Workout() {
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

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMechanic() {
        return mechanic;
    }

    public void setMechanic(String mechanic) {
        this.mechanic = mechanic;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public List<String> getPrimaryMuscles() {
        return primaryMuscles;
    }

    public void setPrimaryMuscles(List<String> primaryMuscles) {
        this.primaryMuscles = primaryMuscles;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Workout{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", force='" + force + '\'' +
               ", level='" + level + '\'' +
               ", mechanic='" + mechanic + '\'' +
               ", equipment='" + equipment + '\'' +
               ", primaryMuscles=" + primaryMuscles +
               ", secondaryMuscles=" + secondaryMuscles +
               ", instructions=" + instructions +
               ", category='" + category + '\'' +
               ", images=" + images +
               '}';
    }
}
