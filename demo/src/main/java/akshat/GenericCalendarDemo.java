package akshat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenericCalendarDemo {
    private static final Path EVENTS_FILE = Path.of("upcoming-events.json");

    public static void main(String[] args) {
        try {
            List<CalendarRecord> loadedRecords = loadRecords();
            RecordStore<CalendarRecord> eventStore = new RecordStore<>();

            loadedRecords.forEach(eventStore::add);

            System.out.println("Calendar events from upcoming-events.json:");
            displayRecords(eventStore.getRecords(), event ->
                    event.getTitle() + " | " + event.getDate() + " | " + event.getNotes());

            System.out.println("\nNumber of records: " + eventStore.size());
        } catch (IOException exception) {
            System.out.println("Unable to read " + EVENTS_FILE + ": " + exception.getMessage());
        }
    }

    private static List<CalendarRecord> loadRecords() throws IOException {
        String json = Files.readString(EVENTS_FILE);
        Type recordListType = new TypeToken<List<CalendarRecord>>() { }.getType();
        List<CalendarRecord> records = new Gson().fromJson(json, recordListType);
        return records == null ? new ArrayList<>() : records;
    }

    private static <T> void displayRecords(List<T> records, Function<T, String> formatter) {
        records.forEach(record -> System.out.println(formatter.apply(record)));
    }

    private static class RecordStore<T> {
        private final List<T> records = new ArrayList<>();

        private void add(T record) {
            records.add(record);
        }

        private List<T> getRecords() {
            return records;
        }

        private int size() {
            return records.size();
        }
    }

    private static class CalendarRecord {
        private String title;
        private String date;
        private String notes;

        private String getTitle() {
            return title;
        }

        private String getDate() {
            return date;
        }

        private String getNotes() {
            return notes == null ? "" : notes;
        }
    }
}
