package net.orisestudios.com;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("el-Cebrü’s-Sâde / Mert Ömer Kapçık / Versiyon'ul-Evvel");
            
            try {
                UIManager.setLookAndFeel( new FlatLightLaf());
            } catch(Exception ex) {
                System.err.println("Failed to initialize LaF");
            }
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(800);
            
            Renderer renderer = new Renderer();
            FunctionPanel functionPanel = new FunctionPanel(renderer);
            
            splitPane.setLeftComponent(renderer);
            splitPane.setRightComponent(functionPanel);
            
            ImageIcon icon = new ImageIcon("res/icon.png");
            
            frame.setIconImage(icon.getImage());
            frame.add(splitPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}