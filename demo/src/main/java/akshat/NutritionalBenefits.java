package akshat;

public interface NutritionalBenefits {
    public void displayNutritionalValue();
    public void displayWarning();
}
    class Banana implements NutritionalBenefits {

    @Override
    public void displayNutritionalValue() {
        System.out.println("Banana: 105 calories, 27g Carbs, 1.3g Protein");
    }
    @Override
    public void displayWarning() {
        System.out.println("Warning: High in sugar. Eat in moderation if monitoring blood sugar.");
    }

}
    
 