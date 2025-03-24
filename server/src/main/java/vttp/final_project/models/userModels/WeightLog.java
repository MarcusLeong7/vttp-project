package vttp.final_project.models.userModels;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WeightLog {

    private Long id;
    private Integer userId;
    private BigDecimal weight;
    private LocalDate date;
    private String notes;

    public WeightLog() {
    }

    public WeightLog(Long id, Integer userId, BigDecimal weight, LocalDate date, String notes) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.date = date;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


}
