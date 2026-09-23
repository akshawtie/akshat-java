package akshat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MusicCrudApp extends JFrame {
    private JTextField songNameField;
    private JTextField artistNameField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private int selectedId = -1;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/music";
    private static final String USER = "root";
    private static final String PASS = "5202";

    public MusicCrudApp() {
        setTitle("Music Database CRUD App");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("Song Name:"));
        songNameField = new JTextField();
        inputPanel.add(songNameField);
        inputPanel.add(new JLabel("Artist Name:"));
        artistNameField = new JTextField();
        inputPanel.add(artistNameField);
        add(inputPanel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[] { "ID", "Song Name", "Artist Name" }, 0);
        table = new JTable(tableModel);

        // Listen for row clicks to populate the fields for Update/Delete
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = (int) tableModel.getValueAt(row, 0);
                songNameField.setText(tableModel.getValueAt(row, 1).toString());
                artistNameField.setText(tableModel.getValueAt(row, 2).toString());
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Bottom Panel: Buttons & Status ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        addButton = new JButton("Add (Create)");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Fields");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addRecord());
        updateButton.addActionListener(e -> updateRecord());
        deleteButton.addActionListener(e -> deleteRecord());
        clearButton.addActionListener(e -> clearFields());

        loadData();
    }

    private void addRecord() {
        String songName = songNameField.getText().trim();
        String artistName = artistNameField.getText().trim();

        if (songName.isEmpty() || artistName.isEmpty()) {
            statusLabel.setText("Status: Fields cannot be empty!");
            return;
        }

        String sql = "INSERT INTO songs (song_name, artist_name) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, songName);
            pstmt.setString(2, artistName);
            pstmt.executeUpdate();

            statusLabel.setText("Status: Record added successfully!");
            clearFields();
            loadData();
        } catch (SQLException ex) {
            statusLabel.setText("Status: Error adding record.");
            ex.printStackTrace();
        }
    }

    // --- READ ---
    private void loadData() {
        tableModel.setRowCount(0); // Clear existing table data
        String sql = "SELECT * FROM songs";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("song_name"));
                row.add(rs.getString("artist_name"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            statusLabel.setText("Status: Error loading data.");
            ex.printStackTrace();
        }
    }

    // --- UPDATE ---
    private void updateRecord() {
        if (selectedId == -1) {
            statusLabel.setText("Status: Please select a record from the table to update.");
            return;
        }

        String songName = songNameField.getText().trim();
        String artistName = artistNameField.getText().trim();

        String sql = "UPDATE songs SET song_name = ?, artist_name = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, songName);
            pstmt.setString(2, artistName);
            pstmt.setInt(3, selectedId);
            pstmt.executeUpdate();

            statusLabel.setText("Status: Record updated successfully!");
            clearFields();
            loadData();
        } catch (SQLException ex) {
            statusLabel.setText("Status: Error updating record.");
            ex.printStackTrace();
        }
    }

    // --- DELETE ---
    private void deleteRecord() {
        if (selectedId == -1) {
            statusLabel.setText("Status: Please select a record from the table to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM songs WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedId);
            pstmt.executeUpdate();

            statusLabel.setText("Status: Record deleted successfully!");
            clearFields();
            loadData();
        } catch (SQLException ex) {
            statusLabel.setText("Status: Error deleting record.");
            ex.printStackTrace();
        }
    }

    // Helper to reset UI state
    private void clearFields() {
        songNameField.setText("");
        artistNameField.setText("");
        selectedId = -1;
        table.clearSelection();
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found in classpath.");
        }

        SwingUtilities.invokeLater(() -> {
            new MusicCrudApp().setVisible(true);
        });
    }
}