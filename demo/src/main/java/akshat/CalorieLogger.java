package akshat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CalorieLogger {
    private static final Path LOG_FILE = Path.of("calorie-log.json");

    public void log(CalorieEntry entry) throws IOException {
        String jsonEntry = String.format(
                "{\n  \"date\": \"%s\",\n  \"name\": \"%s\",\n  \"carbs\": %.2f,\n  \"protein\": %.2f,\n  \"fats\": %.2f,\n  \"totalCalories\": %.2f\n}",
                entry.getDate(),
                entry.getName(),
                entry.getCarbs(),
                entry.getProtein(),
                entry.getFats(),
                entry.getTotalCalories()
        );

        String existing = Files.exists(LOG_FILE)
                ? Files.readString(LOG_FILE, StandardCharsets.UTF_8).trim()
                : "";

        if (existing.isEmpty()) {
            Files.writeString(LOG_FILE, "[\n" + jsonEntry + "\n]", StandardCharsets.UTF_8);
            return;
        }

        String updated;
        if (existing.startsWith("[") && existing.endsWith("]")) {
            String inside = existing.substring(1, existing.length() - 1).trim();
            if (inside.isEmpty()) {
                updated = "[\n" + jsonEntry + "\n]";
            } else {
                updated = "[\n" + inside + ",\n" + jsonEntry + "\n]";
            }
        } else {
            updated = "[\n" + jsonEntry + "\n]";
        }

        Files.writeString(LOG_FILE, updated, StandardCharsets.UTF_8);
    }
}