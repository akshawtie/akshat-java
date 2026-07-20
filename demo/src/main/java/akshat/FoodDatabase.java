import java.util.List;

public class FoodDatabase {
    public List<FoodItem> foods;

    public static class FoodItem {
        public String id;
        public String name;
        public String brand;
        public ServingSize servingSize;
        public int calories;
        public Macros macros;
        public List<String> tags;
    }

    public static class ServingSize {
        public double amount;
        public String unit;
    }

    public static class Macros {
        public double protein;
        public double carbohydrates;
        public double fat;
        public double fiber;
    }
}f