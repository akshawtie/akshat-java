import javax.swing.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;

public class practice2 {
    static final String Dburl = "jdbc:mysql://localhost:3306/practice";
    static final String USER = "root";
    static final String PASS = "5202";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Help");
        frame.setSize(200, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel label1 = new JLabel("Name");
        JTextField inp1 = new JTextField(15);
        JLabel label2 = new JLabel("rollno");
        JTextField inp2 = new JTextField(15);
        JLabel label3 = new JLabel("course");
        JTextField inp3 = new JTextField(15);
        JButton submitb = new JButton("Submit");
        JButton updateb = new JButton("Update");
        JButton deleteb = new JButton("Delete");
        JButton outputb = new JButton("output");
        frame.add(label1);
        frame.add(inp1);
        frame.add(label2);
        frame.add(inp2);
        frame.add(label3);
        frame.add(inp3);
        frame.add(submitb);
        frame.add(updateb);
        frame.add(deleteb);
        frame.add(outputb);
        JTextArea outputarea = new JTextArea(10, 15);
        frame.add(outputarea);
        frame.setVisible(true);

        submitb.addActionListener(e -> {
            String Sql = "insert into students (name,rollno,course) values (?,?,?)";
            System.out.println("action submit");
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    PreparedStatement p1 = conn.prepareStatement(Sql)) {
                p1.setString(1, inp1.getText());
                p1.setInt(2, Integer.parseInt(inp2.getText()));
                p1.setString(3, inp3.getText());
                p1.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Database Error" + ex.getMessage());
            }
        });

        updateb.addActionListener(e -> {
            String Sql = "update students set name=?,course=? where rollno=?";
            System.out.println("action submit");
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    PreparedStatement p1 = conn.prepareStatement(Sql)) {
                p1.setString(1, inp1.getText());
                p1.setInt(3, Integer.parseInt(inp2.getText()));
                p1.setString(2, inp3.getText());
                p1.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Database Error" + ex.getMessage());
            }

        });

        outputb.addActionListener(e -> {

            String Sql = "select * from students";
            outputarea.setText("");
            System.out.println("action submit");
            try (Connection conn = DriverManager.getConnection(Dburl, USER, PASS);
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(Sql)) {
                while (rs.next()) {
                    outputarea.append(rs.getString("name") + " | " +
                            rs.getInt("rollno") + " | " +
                            rs.getString("course") + "\n");
                }
            }

            catch (SQLException ex) {
                System.out.println("Database Error" + ex.getMessage());
            }

        });

    }
}
