package org.shcherbakov.GUI;

import org.shcherbakov.ResourceAllocator;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.List;

public class ResourceAllocatorGUI extends JFrame {

    private final JTable profitTable;
    private final JTextArea resultArea;
    private final JComboBox<Integer> rowsComboBox;
    private final JComboBox<Integer> columnsComboBox;
    private final MyDefaultTableModel tableModel;

    public ResourceAllocatorGUI() {
        setTitle("Распределение ресурсов между предприятиями");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Закрытие приложения
        setLayout(new BorderLayout());

        // Set application icon
        URL iconURL = ResourceAllocatorGUI.class.getClassLoader().getResource("icon.png");
        setIconImage(Toolkit.getDefaultToolkit().getImage(iconURL));

        JPanel controlPanel = new JPanel();

        rowsComboBox = new JComboBox<>(new Integer[]{5, 6, 7, 8, 9, 10});
        rowsComboBox.setSelectedItem(10);
        rowsComboBox.addActionListener(new TableUpdateListener());

        columnsComboBox = new JComboBox<>(new Integer[]{2, 3, 4});
        columnsComboBox.setSelectedItem(4);
        columnsComboBox.addActionListener(new TableUpdateListener());

        controlPanel.add(new JLabel("Количество ресурсов (строки):"));
        controlPanel.add(rowsComboBox);
        controlPanel.add(new JLabel("Количество предприятий (столбцы):"));
        controlPanel.add(columnsComboBox);

        JButton solveButton = new JButton("Решить");
        solveButton.addActionListener(new SolveButtonListener());
        controlPanel.add(solveButton);

        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        // Table setup using MyDefaultTableModel
        int initialRows = (Integer) rowsComboBox.getSelectedItem();
        int initialColumns = (Integer) columnsComboBox.getSelectedItem();
        tableModel = new MyDefaultTableModel(initialRows, initialColumns + 1);

        profitTable = new JTable(tableModel);
        tableModel.setCustomCellEditor(profitTable);
        profitTable.setRowHeight(30);
        profitTable.setFont(new Font("Roboto", Font.PLAIN, 14));

        JScrollPane tableScrollPane = new JScrollPane(profitTable);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        updateTable();

        add(controlPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(resultScrollPane, BorderLayout.SOUTH);

        createMenuBar();

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem importItem = new JMenuItem("Импортировать");
        importItem.addActionListener(new ImportActionListener());
        JMenuItem exportItem = new JMenuItem("Экспортировать");
        exportItem.addActionListener(new ExportActionListener());

        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private void updateTable() {
        int rows = (Integer) rowsComboBox.getSelectedItem();
        int columns = (Integer) columnsComboBox.getSelectedItem();

        tableModel.setRowCount(rows);
        tableModel.setColumnCount(columns + 1);

        tableModel.updateColumnIdentifiers(columns);
        tableModel.updateRowHeaders();

        // Set header font
        JTableHeader header = profitTable.getTableHeader();
        header.setFont(new Font("OpenSans", Font.BOLD, 14));
    }

    private class TableUpdateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateTable();
        }
    }

    private class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int rows = (Integer) rowsComboBox.getSelectedItem();
            int columns = (Integer) columnsComboBox.getSelectedItem();
            double[][] profit = new double[rows][columns];

            for (int i = 0; i < rows; i++) {
                for (int j = 1; j <= columns; j++) {
                    try {
                        profit[i][j - 1] = Double.parseDouble((String) tableModel.getValueAt(i, j));
                    } catch (NumberFormatException ex) {
                        profit[i][j - 1] = 0;
                    }
                }
            }

            ResourceAllocator allocator = new ResourceAllocator(profit);
            allocator.findMaxProfit();

            resultArea.setText("Максимальная прибыль: " + allocator.getMaxProfit() + "\n");
            resultArea.append("Возможные распределения ресурсов:\n");

            List<int[]> solutions = allocator.getSolutions();
            for (int[] allocation : solutions) {
                for (int i = 0; i < allocation.length; i++) {
                    resultArea.append("Предприятию " + (i + 1) + " выделено " + allocation[i] + " ресурсов.\n");
                }
                resultArea.append("\n");
            }
        }
    }

    // Автоопределение строк и столбцов при импорте
    private class ImportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(ResourceAllocatorGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int row = 0;
                    int columns = -1; // Определение количества столбцов
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        if (columns == -1) {
                            columns = values.length;
                            columnsComboBox.setSelectedItem(columns - 1);
                        }
                        tableModel.setRowCount(row + 1); // Добавляем новую строку при необходимости
                        for (int col = 0; col < values.length; col++) {
                            tableModel.setValueAt(values[col], row, col);
                        }
                        row++;
                    }
                    rowsComboBox.setSelectedItem(row); // Установка количества строк
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ResourceAllocatorGUI.this, "Ошибка чтения файла.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Экспорт данных в текстовый файл
    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(ResourceAllocatorGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath() + ".txt"))) {
                    int rows = tableModel.getRowCount();
                    int columns = tableModel.getColumnCount();
                    for (int i = 0; i < rows; i++) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < columns; j++) {
                            if (j > 0) sb.append(",");
                            sb.append(tableModel.getValueAt(i, j));
                        }
                        bw.write(sb.toString());
                        bw.newLine();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ResourceAllocatorGUI.this, "Ошибка записи файла.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        new ResourceAllocatorGUI();
    }
}
