package akshat;

import java.time.LocalDate;
public class CalorieEntry {
    private final String date;
    private final String name;
    private final double carbs;
    private final double protein;
    private final double fats;
    private final double totalCalories;
    public CalorieEntry(String name,double carbs,double protein,double fats,double totalCalories) {
        this.date = LocalDate.now().toString();
        this.name = name;
        this.carbs = carbs;
        this.protein = protein;
        this.fats = fats;
        this.totalCalories = totalCalories;
    }

    public String getDate() {
        return date;
    }
    public String getName() {
        return name;
    }
    public double getCarbs() {
        return carbs;
    }
    public double getProtein() {
        return protein;
    }
    public double getFats() {
        return fats;
    }
    public double getTotalCalories() {
        return totalCalories;
    }
}