package akshat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarEventManager {
    private static final Path EVENTS_FILE = Path.of("upcoming-events.json");
    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "\\{\\s*\"title\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*"
                    + "\"date\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*"
                    + "\"notes\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*\\}",
            Pattern.DOTALL
    );

    private final List<Event> events;

    public CalendarEventManager() throws IOException {
        this.events = new ArrayList<>();
        loadEvents();
    }

    public static void main(String[] args) {
        try {
            CalendarEventManager manager = new CalendarEventManager();
            manager.runInteractive();
        } catch (IOException e) {
            System.out.println("Failed to load events: " + e.getMessage());
        }
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addEvent(String title, String date, String notes) {
        events.add(new Event(title, date, notes));
    }

    public boolean deleteEvent(int index) {
        if (index < 0 || index >= events.size()) {
            return false;
        }
        events.remove(index);
        return true;
    }

    public void saveEvents() throws IOException {
        String json = toJson(events);
        Files.writeString(EVENTS_FILE, json + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public void runInteractive() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\nCalendar Menu");
                System.out.println("1. View events");
                System.out.println("2. Add event");
                System.out.println("3. Delete event");
                System.out.println("4. Exit and save");
                System.out.println("5. Demonstrate stream operations");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        printEvents();
                        break;
                    case "2":
                        System.out.print("Event title: ");
                        String title = scanner.nextLine().trim();
                        System.out.print("Event date (e.g. 2026-12-31 18:00): ");
                        String date = scanner.nextLine().trim();
                        System.out.print("Notes: ");
                        String notes = scanner.nextLine().trim();

                        if (title.isEmpty() || date.isEmpty()) {
                            System.out.println("Title and date are required.");
                            break;
                        }

                        addEvent(title, date, notes);
                        System.out.println("Event added.");
                        break;
                    case "3":
                        printEvents();
                        if (events.isEmpty()) {
                            break;
                        }
                        System.out.print("Enter event number to delete: ");
                        String rawIndex = scanner.nextLine().trim();
                        try {
                            int displayIndex = Integer.parseInt(rawIndex);
                            boolean removed = deleteEvent(displayIndex - 1);
                            if (removed) {
                                System.out.println("Event deleted.");
                            } else {
                                System.out.println("Invalid event number.");
                            }
                        } catch (NumberFormatException nfe) {
                            System.out.println("Please enter a valid number.");
                        }
                        break;
                    case "4":
                        saveEvents();
                        System.out.println("Events saved to upcoming-events.json.");
                        running = false;
                        break;
                    case "5":
                        demonstrateStreamOperations();
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-5.");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to save events: " + e.getMessage());
        }
    }

        private void demonstrateStreamOperations() {
        if (events.isEmpty()) {
            System.out.println("No events available for the stream demonstration.");
            return;
        }

        System.out.println("\nEvents with notes (filter):");
        events.stream()
            .filter(event -> !event.getNotes().isEmpty())
            .forEach(event -> System.out.println(event.getTitle() + " - " + event.getNotes()));

        System.out.println("\nEvents sorted by date (sorted):");
        events.stream()
            .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getTitle))
            .forEach(event -> System.out.println(event.getDate() + " - " + event.getTitle()));

        System.out.println("\nDistinct event dates (distinct):");
        events.stream()
            .map(Event::getDate)
            .distinct()
            .forEach(System.out::println);

        System.out.println("\nFirst three events (limit):");
        events.stream()
            .limit(3)
            .forEach(event -> System.out.println(event.getTitle() + " - " + event.getDate()));

        long eventCount = events.stream().count();
        double averageNoteLength = events.stream()
            .mapToInt(event -> event.getNotes().length())
            .average()
            .orElse(0.0);
        Event earliestEvent = events.stream()
            .min(Comparator.comparing(Event::getDate))
            .orElse(null);
        Event latestEvent = events.stream()
            .max(Comparator.comparing(Event::getDate))
            .orElse(null);

        System.out.println("\nCalculations:");
        System.out.println("Total events (count): " + eventCount);
        System.out.printf("Average note length (average): %.2f characters%n", averageNoteLength);
        System.out.println("Earliest event (min): " + earliestEvent.getDate() + " - " + earliestEvent.getTitle());
        System.out.println("Latest event (max): " + latestEvent.getDate() + " - " + latestEvent.getTitle());
        }

    private void loadEvents() throws IOException {
        if (!Files.exists(EVENTS_FILE)) {
            Files.writeString(EVENTS_FILE, "[]" + System.lineSeparator(), StandardCharsets.UTF_8);
            return;
        }

        String content = Files.readString(EVENTS_FILE, StandardCharsets.UTF_8).trim();
        if (content.isEmpty()) {
            return;
        }

        events.clear();
        Matcher matcher = EVENT_PATTERN.matcher(content);
        while (matcher.find()) {
            String title = unescapeJson(matcher.group(1));
            String date = unescapeJson(matcher.group(2));
            String notes = unescapeJson(matcher.group(3));
            events.add(new Event(title, date, notes));
        }
    }

    private String toJson(List<Event> eventList) {
        if (eventList.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        for (int i = 0; i < eventList.size(); i++) {
            Event event = eventList.get(i);
            builder.append("  {\n");
            builder.append("    \"title\": \"").append(escapeJson(event.getTitle())).append("\",\n");
            builder.append("    \"date\": \"").append(escapeJson(event.getDate())).append("\",\n");
            builder.append("    \"notes\": \"").append(escapeJson(event.getNotes())).append("\"\n");
            builder.append("  }");
            if (i < eventList.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        return escaped;
    }

    private String unescapeJson(String value) {
        if (value == null) {
            return "";
        }

        String unescaped = value;
        unescaped = unescaped.replace("\\n", "\n");
        unescaped = unescaped.replace("\\r", "\r");
        unescaped = unescaped.replace("\\t", "\t");
        unescaped = unescaped.replace("\\\"", "\"");
        unescaped = unescaped.replace("\\\\", "\\");
        return unescaped;
    }

    private void printEvents() {
        if (events.isEmpty()) {
            System.out.println("No upcoming events found.");
            return;
        }

        System.out.println("Upcoming events:");
        IntStream.range(0, events.size())
                .mapToObj(index -> {
                    Event event = events.get(index);
                    return (index + 1) + ". " + event.getTitle() + " | " + event.getDate() + " | " + event.getNotes();
                })
                .forEach(System.out::println);
    }

    public static class Event {
        private final String title;
        private final String date;
        private final String notes;

        public Event(String title, String date, String notes) {
            this.title = title;
            this.date = date;
            this.notes = notes == null ? "" : notes;
        }

        public String getTitle() {
            return title;
        }

        public String getDate() {
            return date;
        }

        public String getNotes() {
            return notes;
        }
    }
}
