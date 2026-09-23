import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;

public class practice {
    static final String Dburl = "jdbc:mysql://localhost:3306/practice";
    static final String USER = "root";
    static final String PASS = "5202";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Windossw");

        frame.setSize(500, 400);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel label1 = new JLabel("Input");
        JTextField inp1 = new JTextField(10);
        JLabel label2 = new JLabel("Roll");
        JTextField inp2 = new JTextField(10);
        JLabel label3 = new JLabel("course");
        JTextField inp3 = new JTextField(10);

        JButton buttons = new JButton("Submit");
        JButton buttonU = new JButton("Uupdate");
        JButton buttonD = new JButton("Delete");
        JButton buttonO = new JButton("Output");
        JTextArea outputarea = new JTextArea(30, 50);

        frame.add(label1);
        frame.add(inp1);
        frame.add(label2);
        frame.add(inp2);
        frame.add(label3);
        frame.add(inp3);

        frame.add(buttons);
        frame.add(buttonU);
        frame.add(buttonD);
        frame.add(buttonO);

        frame.add(outputarea);
        frame.setVisible(true);

        buttons.addActionListener(e -> {
            String Sql = "insert into students (name,rollno,course) values(?,?,?)";
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    PreparedStatement p1 = conn.prepareStatement(Sql)) {
                p1.setString(1, inp1.getText());
                p1.setInt(2, Integer.parseInt(inp2.getText()));
                p1.setString(3, inp3.getText());
                p1.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Data Inserted Successfully!");

            } catch (SQLException ex) {
                System.out.println("SQLE exception Database error" + ex.getMessage());
            }
        });

        buttonD.addActionListener(e -> {
            String Sql = "Delete from students where rollno=?";
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    PreparedStatement p1 = conn.prepareStatement(Sql)) {
                p1.setInt(1, Integer.parseInt(inp2.getText()));
                p1.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Data Deleted Successfully!");

            } catch (SQLException ex) {
                System.out.println("SQLE exception Database error" + ex.getMessage());
            }
        });

        buttonO.addActionListener(e -> {
            String Sql = "select * from students";
            outputarea.setText("");
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(Sql)) {
                while (rs.next()) {
                    outputarea.append(rs.getString("Name") + "|" + rs.getInt("rollno") + "|" + rs.getString("course"));

                }

            } catch (SQLException ex) {
                System.out.println("SQLE exception Database error" + ex.getMessage());
            }
        });

    }

}