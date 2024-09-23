package org.shcherbakov.GUI;

import org.shcherbakov.ResourceAllocator;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Закрытие приложения
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Set application icon with error handling
        URL iconURL = ResourceAllocatorGUI.class.getClassLoader().getResource("icon.png");
        if (iconURL != null) {
            setIconImage(Toolkit.getDefaultToolkit().getImage(iconURL));
        } else {
            JOptionPane.showMessageDialog(this, "Иконка приложения не найдена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

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

        JMenu helpMenu = new JMenu("Справка");
        JMenuItem aboutUsItem = new JMenuItem("О нас");
        aboutUsItem.addActionListener(new AboutUsActionListener());
        JMenuItem helpItem = new JMenuItem("Инструкция");
        helpItem.addActionListener(new HelpActionListener());

        fileMenu.add(importItem);
        fileMenu.add(exportItem);

        helpMenu.add(aboutUsItem);
        helpMenu.add(helpItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

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

            StringBuilder errorMessages = new StringBuilder(); // Хранение всех сообщений об ошибках

            for (int i = 0; i < rows; i++) {
                for (int j = 1; j <= columns; j++) {
                    try {
                        String value = (String) tableModel.getValueAt(i, j);
                        if (value == null || value.isEmpty()) {
                            throw new NumberFormatException(); // Бросаем исключение при пустом вводе
                        }
                        profit[i][j - 1] = Double.parseDouble(value);
                    } catch (NumberFormatException ex) {
                        profit[i][j - 1] = 0;
                        // Добавляем сообщение об ошибке в StringBuilder
                        errorMessages.append("Неверный ввод в ячейке [")
                                .append(i + 1)
                                .append(", ")
                                .append(j)
                                .append("].\n");
                    }
                }
            }

            // Проверяем, есть ли сообщения об ошибках
            if (!errorMessages.isEmpty()) {
                // Создаем JTextArea с ошибками
                JTextArea textArea = new JTextArea(10, 50);  // 10 строк, 50 символов
                textArea.setText(errorMessages.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                // Оборачиваем JTextArea в JScrollPane
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 200));  // Ограничение размеров окна

                // Показываем все сообщения об ошибках в прокручиваемом окне
                JOptionPane.showMessageDialog(ResourceAllocatorGUI.this, scrollPane, "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            } else {
                // Если ошибок нет, продолжаем решение задачи
                ResourceAllocator allocator = new ResourceAllocator(profit);
                allocator.findMaxProfit();
                displayResults(allocator);
            }
        }


        private void displayResults(ResourceAllocator allocator) {
            resultArea.setText("Максимальная прибыль: " + allocator.getMaxProfit() + "\n");
            resultArea.append("Возможные распределения ресурсов:\n");
            List<int[]> solutions = allocator.getSolutions();
            int j = 1;
            for (int[] allocation : solutions) {
                resultArea.append(String.format("Решение №%d\n", j));
                j++;
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

    // AboutUsActionListener opens a browser with the GitHub link
    private static class AboutUsActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/SharkDeve1oper/CourseProject2024"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class HelpActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Create a new frame for the help window
            JFrame helpFrame = new JFrame("Справка");
            helpFrame.setLocationRelativeTo(null);

            // Set application icon with error handling
            URL iconURL = ResourceAllocatorGUI.class.getClassLoader().getResource("icon.png");
            if (iconURL != null) {
                helpFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(iconURL));
            } else {
                JOptionPane.showMessageDialog(helpFrame, "Иконка приложения не найдена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

            helpFrame.setSize(800, 600);
            helpFrame.setMinimumSize(new Dimension(800, 600));
            helpFrame.setLayout(new BorderLayout());
            helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Create a text area with instructions
            JTextArea helpText = new JTextArea();

            helpText.setText("""
                Инструкция по использованию приложения:
                1 шаг: Установите количество ресурсов, которые могут быть распределены между процессами.
                2 шаг: Установите количество процессов, в которые можно вложить ресурсы.
                3 шаг: Введите доходность каждого процесса в зависимости от вложенных в него ресурсов.
                4 шаг: Нажмите кнопку "Решить".
                5 шаг: Получите решение — оптимальное распределение ресурсов и итоговый результат.
        """);
            helpText.setFont(new Font("OpenSans", Font.PLAIN, 14));
            helpText.setEditable(false);

            // Load and scale the image to 300x300
            URL imageURL = ResourceAllocatorGUI.class.getClassLoader().getResource("help.png");
            if (imageURL != null) {
                ImageIcon originalIcon = new ImageIcon(imageURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(500, 400, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                // Create an image label with scaled icon
                JLabel imageLabel = new JLabel(scaledIcon);

                // Add components to the frame
                helpFrame.add(new JScrollPane(helpText), BorderLayout.CENTER);
                helpFrame.add(imageLabel, BorderLayout.SOUTH);
            } else {
                JOptionPane.showMessageDialog(helpFrame, "Изображение помощи не найдено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

            // Display the frame
            helpFrame.setVisible(true);
        }
    }


    public static void main(String[] args) {
        new ResourceAllocatorGUI();
    }
}
