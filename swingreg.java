import javax.swing.*;
import java.awt.*;

class swingreg {

    public static void main(String args[]) {
        JFrame frame = new JFrame();

        JLabel message;
        JLabel nameLabel, genderLabel;
        JTextField nameField;
        JRadioButton genderMale, genderFemale;
        JLabel mobileNoLabel;
        JTextField mobileNoField;
        JLabel semesterLabel;
        JComboBox<Integer> semesterList;
        JButton registerButton;

        message = new JLabel("Register a new Student");
        message.setFont(new Font("Courier", Font.BOLD, 20));

        nameLabel = new JLabel("Name");
        nameField = new JTextField();

        genderLabel = new JLabel("Gender");
        genderMale = new JRadioButton("Male", true);
        genderFemale = new JRadioButton("Female");
        
        // Group the radio buttons so only one can be selected at a time
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(genderMale);
        genderGroup.add(genderFemale);

        mobileNoLabel = new JLabel("Mobile No");
        mobileNoField = new JTextField();

        semesterLabel = new JLabel("Semester");
        semesterList = new JComboBox<>();
        for (int i = 1; i <= 8; i++) {
            semesterList.addItem(i);
        }

        registerButton = new JButton("Register");

        frame.setLayout(null);

        message.setBounds(50, 10, 600, 30);
        nameLabel.setBounds(50, 60, 100, 30);
        nameField.setBounds(130, 60, 200, 30);
        genderLabel.setBounds(50, 160, 100, 30);
        genderMale.setBounds(130, 160, 100, 30);
        genderFemale.setBounds(240, 160, 100, 30);

        mobileNoLabel.setBounds(50, 260, 100, 30);
        mobileNoField.setBounds(130, 260, 200, 30);

        semesterLabel.setBounds(50, 310, 100, 30);
        semesterList.setBounds(130, 310, 200, 30);
        registerButton.setBounds(130, 390, 200, 30);

        frame.add(message);
        frame.add(nameLabel);
        frame.add(nameField);

        frame.add(genderLabel);
        frame.add(genderMale);
        frame.add(genderFemale);

        frame.add(mobileNoLabel);
        frame.add(mobileNoField);

        frame.add(semesterLabel);
        frame.add(semesterList);
        frame.add(registerButton);

        frame.setSize(500, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
    }
}
