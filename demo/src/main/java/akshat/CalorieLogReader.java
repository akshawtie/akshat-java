package akshat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalorieLogReader {
    private static final Path LOG_FILE = Path.of("calorie-log.json");
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "\\{\\s*\"date\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*"
                    + "\"name\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*"
                    + "\"carbs\"\\s*:\\s*([0-9.]+)\\s*,\\s*"
                    + "\"protein\"\\s*:\\s*([0-9.]+)\\s*,\\s*"
                    + "\"fats\"\\s*:\\s*([0-9.]+)\\s*,\\s*"
                    + "\"totalCalories\"\\s*:\\s*([0-9.]+)\\s*\\}",
            Pattern.DOTALL
    );

    public CalorieLogEntry[] loadEntries() throws IOException {
        if (!Files.exists(LOG_FILE)) {
            return new CalorieLogEntry[0];
        }

        String content = Files.readString(LOG_FILE, StandardCharsets.UTF_8);
        Matcher matcher = ENTRY_PATTERN.matcher(content);
        List<CalorieLogEntry> entries = new ArrayList<>();

        while (matcher.find()) {
            entries.add(new CalorieLogEntry(
                    matcher.group(1),
                    matcher.group(2),
                    Double.parseDouble(matcher.group(3)),
                    Double.parseDouble(matcher.group(4)),
                    Double.parseDouble(matcher.group(5)),
                    Double.parseDouble(matcher.group(6))
            ));
        }

        return entries.toArray(new CalorieLogEntry[0]);
    }

    public static class CalorieLogEntry {
        private final String date;
        private final String name;
        private final double carbs;
        private final double protein;
        private final double fats;
        private final double totalCalories;

        public CalorieLogEntry(String date, String name, double carbs, double protein, double fats, double totalCalories) {
            this.date = date;
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
}