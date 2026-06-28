package akshat;
import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            System.out.print("Enter carbs in grams: ");
            double carbs = scanner.nextDouble();
            System.out.print("Enter protein in grams: ");
            double protein = scanner.nextDouble();
            System.out.print("Enter fats in grams: ");
            double fats = scanner.nextDouble();
            double totalCalories = (carbs * 4) + (protein * 4) + (fats * 9);


            // the magic happens here.

            CalorieEntry entry = new CalorieEntry(name, carbs, protein, fats, totalCalories);
            CalorieLogger logger = new CalorieLogger();
            logger.log(entry);
            System.out.println("Total calories: " + totalCalories);
            System.out.println("Saved to calorie-log.json");


            ///lab 2
            ///written by ai 
            System.out.print("Do you want to see previous history? (yes/no): ");
            String answer = scanner.next().trim().toLowerCase();

            if (answer.equals("yes") || answer.equals("y")) {
                CalorieLogReader reader = new CalorieLogReader();
                CalorieLogReader.CalorieLogEntry[] entries = reader.loadEntries();

                if (entries.length == 0) {
                    System.out.println("No previous history found.");
                } else {
                    System.out.println("Previous history:");
                    for (CalorieLogReader.CalorieLogEntry logEntry : entries) {
                        System.out.println(
                                logEntry.getDate() + " | " +
                                logEntry.getName() + " | " +
                                "carbs=" + logEntry.getCarbs() + " | " +
                                "protein=" + logEntry.getProtein() + " | " +
                                "fats=" + logEntry.getFats() + " | " +
                                "totalCalories=" + logEntry.getTotalCalories()
                        );
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to write calorie log: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Please enter valid numbers.");
        }
    }
}