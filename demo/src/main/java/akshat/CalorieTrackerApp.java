package akshat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalorieTrackerApp extends JFrame {

    private static final String JSON_FILE_PATH = "/run/media/awkwart/New Volume/JAVA/calorie-log.json";

    private List<LogEntry> logs = new ArrayList<>();

    private JTextField txtName;
    private JTextField txtCarbs;
    private JTextField txtProtein;
    private JTextField txtFats;
    
    private Gson gson;

    public CalorieTrackerApp() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadData();
        
        setTitle("Calorie Tracker - Add Log");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Use a null layout for absolute positioning, just like your swingreg example!
        // This prevents the components from stretching to occupy the whole screen.
        setLayout(null);

        JLabel titleLabel = new JLabel("Enter Calorie Macros");
        titleLabel.setFont(new Font("Courier", Font.BOLD, 18));
        titleLabel.setBounds(50, 10, 300, 30);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(50, 60, 100, 30);
        txtName = new JTextField();
        txtName.setBounds(140, 60, 180, 30);
        
        JLabel carbsLabel = new JLabel("Carbs (g):");
        carbsLabel.setBounds(50, 110, 100, 30);
        txtCarbs = new JTextField("0");
        txtCarbs.setBounds(140, 110, 180, 30);
        
        JLabel proteinLabel = new JLabel("Protein (g):");
        proteinLabel.setBounds(50, 160, 100, 30);
        txtProtein = new JTextField("0");
        txtProtein.setBounds(140, 160, 180, 30);
        
        JLabel fatsLabel = new JLabel("Fats (g):");
        fatsLabel.setBounds(50, 210, 100, 30);
        txtFats = new JTextField("0");
        txtFats.setBounds(140, 210, 180, 30);
        
        JButton btnAdd = new JButton("Add Log");
        btnAdd.setBounds(140, 260, 180, 30);
        btnAdd.addActionListener(e -> addLog());
        
        // Add all components to the frame
        add(titleLabel);
        add(nameLabel);
        add(txtName);
        add(carbsLabel);
        add(txtCarbs);
        add(proteinLabel);
        add(txtProtein);
        add(fatsLabel);
        add(txtFats);
        add(btnAdd);
    }

    private void loadData() {
        File file = new File(JSON_FILE_PATH);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                java.lang.reflect.Type listType = new TypeToken<ArrayList<LogEntry>>() {}.getType();
                List<LogEntry> data = gson.fromJson(reader, listType);
                if (data != null) {
                    logs = data;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveData() {
        try (Writer writer = new FileWriter(JSON_FILE_PATH)) {
            gson.toJson(logs, writer);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addLog() {
        try {
            String date = java.time.LocalDate.now().toString();
            String name = txtName.getText();
            double carbs = Double.parseDouble(txtCarbs.getText());
            double protein = Double.parseDouble(txtProtein.getText());
            double fats = Double.parseDouble(txtFats.getText());
            
            // Calculate total calories
            double total = (carbs * 4) + (protein * 4) + (fats * 9);
            
            LogEntry entry = new LogEntry(date, name, carbs, protein, fats, total);
            logs.add(entry);
            
            // Save to JSON
            saveData();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Log added successfully!\nTotal Calories: " + total, "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear form
            txtName.setText("");
            txtCarbs.setText("0");
            txtProtein.setText("0");
            txtFats.setText("0");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Carbs, Protein, and Fats.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalorieTrackerApp app = new CalorieTrackerApp();
            app.setLocationRelativeTo(null); // Center on screen
            app.setVisible(true);
        });
    }

    static class LogEntry {
        String date;
        String name;
        double carbs;
        double protein;
        double fats;
        double totalCalories;

        public LogEntry() {}

        public LogEntry(String date, String name, double carbs, double protein, double fats, double totalCalories) {
            this.date = date;
            this.name = name;
            this.carbs = carbs;
            this.protein = protein;
            this.fats = fats;
            this.totalCalories = totalCalories;
        }
    }
}
