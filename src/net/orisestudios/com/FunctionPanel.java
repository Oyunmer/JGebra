package net.orisestudios.com;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import net.orisestudios.com.func.CubicFunction;
import net.orisestudios.com.func.Function;
import net.orisestudios.com.func.FunctionParser;
import net.orisestudios.com.func.LinearFunction;
import net.orisestudios.com.func.QuadraticFunction;

public class FunctionPanel extends JPanel {
    private Renderer renderer;
    private JTextArea functionInput;
    private JButton addButton;
    private JList<String> functionList;
    private DefaultListModel<String> listModel;
    
    public FunctionPanel(Renderer renderer) {
        this.renderer = renderer;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 700));

        JPanel inputPanel = new JPanel(new BorderLayout());
        functionInput = new JTextArea(3, 20);
        functionInput.setLineWrap(true);
        functionInput.setBorder(BorderFactory.createTitledBorder("Fonksiyon Giriniz"));
        
        addButton = new JButton("Ekle");
        addButton.addActionListener(this::addFunction);
        
        inputPanel.add(new JScrollPane(functionInput), BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.SOUTH);
        
        // Fonksiyon listesi
        listModel = new DefaultListModel<>();
        functionList = new JList<>(listModel);
        functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        functionList.setBorder(BorderFactory.createTitledBorder("Fonksiyonlar"));
        
        // Sil butonu
        JButton removeButton = new JButton("Sil");
        removeButton.addActionListener(e -> removeSelectedFunction());
        
        // Panel düzeni
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(functionList), BorderLayout.CENTER);
        add(removeButton, BorderLayout.SOUTH);
    }
    
    private void addFunction(ActionEvent e) {
        String input = functionInput.getText().trim();
        if (!input.isEmpty()) {
            try {
                Function function = FunctionParser.parse(input);
                renderer.addFunction(function);
                listModel.addElement(function.toString());
                functionInput.setText("");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), 
                    "Geçersiz Fonksiyon", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeSelectedFunction() {
        int selectedIndex = functionList.getSelectedIndex();
        if (selectedIndex != -1) {
            renderer.removeFunction(selectedIndex);
            listModel.remove(selectedIndex);
        }
    }
    
    private Function parseFunction(String input) {
        // Basit fonksiyon parsing işlemi
        input = input.replaceAll("\\s+", "").toLowerCase();
        
        // Doğrusal fonksiyon: y = ax + b
        Pattern linearPattern = Pattern.compile("y=([\\d.-]*)x([+\\-][\\d.]+)?");
        Matcher linearMatcher = linearPattern.matcher(input);
        
        // İkinci derece: y = ax² + bx + c
        Pattern quadraticPattern = Pattern.compile("y=([\\d.-]*)x²([+\\-][\\d.]*)x?([+\\-][\\d.]+)?");
        Matcher quadraticMatcher = quadraticPattern.matcher(input);
        
        // Üçüncü derece: y = ax³ + bx² + cx + d
        Pattern cubicPattern = Pattern.compile("y=([\\d.-]*)x³([+\\-][\\d.]*)x²?([+\\-][\\d.]*)x?([+\\-][\\d.]+)?");
        Matcher cubicMatcher = cubicPattern.matcher(input);
        
        try {
            if (linearMatcher.matches()) {
                double a = linearMatcher.group(1).isEmpty() ? 1 : Double.parseDouble(linearMatcher.group(1));
                double b = linearMatcher.group(2) == null ? 0 : Double.parseDouble(linearMatcher.group(2));
                return new LinearFunction(a, b);
            } else if (quadraticMatcher.matches()) {
                double a = quadraticMatcher.group(1).isEmpty() ? 1 : Double.parseDouble(quadraticMatcher.group(1));
                double b = quadraticMatcher.group(2) == null ? 0 : Double.parseDouble(quadraticMatcher.group(2));
                double c = quadraticMatcher.group(3) == null ? 0 : Double.parseDouble(quadraticMatcher.group(3));
                return new QuadraticFunction(a, b, c);
            } else if (cubicMatcher.matches()) {
                double a = cubicMatcher.group(1).isEmpty() ? 1 : Double.parseDouble(cubicMatcher.group(1));
                double b = cubicMatcher.group(2) == null ? 0 : Double.parseDouble(cubicMatcher.group(2));
                double c = cubicMatcher.group(3) == null ? 0 : Double.parseDouble(cubicMatcher.group(3));
                double d = cubicMatcher.group(4) == null ? 0 : Double.parseDouble(cubicMatcher.group(4));
                return new CubicFunction(a, b, c, d);
            } else {
                throw new IllegalArgumentException("Fonksiyon formatı tanınmadı");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Geçersiz sayı formatı");
        }
    }
}