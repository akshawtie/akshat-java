import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SwingDBApp extends JFrame {

    private JTextField nameField;
    private JButton addButton;
    private JLabel statusLabel;

    // Using the database connection details from javaconnection.java
    private static final String DB_URL = "jdbc:mysql://localhost:3306/akshat";
    private static final String USERNAME = "akshat";
    private static final String PASSWORD = "5202";

    // TODO: Update this to match your actual table name if it's different
    private static final String TABLE_NAME = "your_table_name";

    public SwingDBApp() {
        setTitle("Add Name to MariaDB");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Setup the input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField(20);
        inputPanel.add(nameField);

        addButton = new JButton("Add to Table");
        inputPanel.add(addButton);

        // Setup the status label
        statusLabel = new JLabel("Status: Waiting for input", SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);

        // Add padding around the edges
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        paddingPanel.add(inputPanel, BorderLayout.CENTER);
        paddingPanel.add(statusLabel, BorderLayout.SOUTH);

        add(paddingPanel);

        // Add button click listener
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    updateStatus("Error: Name cannot be empty!", Color.RED);
                    return;
                }

                // Add to database
                insertName(name);
            }
        });

        // Allow pressing Enter in the text field to trigger the button
        nameField.addActionListener(e -> addButton.doClick());
    }

    private void insertName(String name) {
        String query = "INSERT INTO " + TABLE_NAME + " (names) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                updateStatus("Success: Added '" + name + "' to the database.", new Color(0, 150, 0));
                nameField.setText(""); // clear field
                nameField.requestFocus();
            } else {
                updateStatus("Error: No rows affected.", Color.RED);
            }

        } catch (SQLException ex) {
            updateStatus("Database Error: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new SwingDBApp().setVisible(true);
        });
    }
}
