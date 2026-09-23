import javax.swing.*;
import java.awt.FlowLayout;

class registration { 
    public static void main(String args[]) { 
        // Create the main window frame
        JFrame f = new JFrame("Radio gaga"); 
        
        // Set a layout manager so components don't overlap each other
        f.setLayout(new FlowLayout());
        
        // Create the components
        JLabel l1 = new JLabel("mwah"); 
        JButton button = new JButton("Touch me"); 
        
        // Add components to the frame
        f.add(l1); 
        f.add(button); 
        
        // Set window size, close behavior, and make it visible
        f.setSize(300, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
