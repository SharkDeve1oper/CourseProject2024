package org.shcherbakov.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDefaultTableModel extends DefaultTableModel {

    public MyDefaultTableModel(int rows, int columns) {
        setRowCount(rows);
        setColumnCount(columns);
    }

    // Custom method to update column headers
    public void updateColumnIdentifiers(int columns) {
        String[] columnNames = new String[columns + 1];
        columnNames[0] = "Вл.Ресурсов";
        for (int i = 1; i <= columns; i++) {
            columnNames[i] = "φ(" + i + ")";
        }
        setColumnIdentifiers(columnNames);
    }

    // Ensure that only the first column is uneditable
    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    // Custom cell editor to handle decimal input formatting
    public void setCustomCellEditor(JTable table) {
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            @Override
            public Object getCellEditorValue() {
                String text = super.getCellEditorValue().toString();
                return formatDecimal(text);
            }

            private String formatDecimal(String text) {
                Pattern pattern = Pattern.compile("^(\\d+)(\\.\\d*)?$");
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    String integerPart = matcher.group(1);
                    String decimalPart = matcher.group(2);
                    return integerPart + Objects.requireNonNullElse(decimalPart, ".0");
                }
                return "0.0"; // Default value if input is invalid
            }

            @Override
            public boolean stopCellEditing() {
                return super.stopCellEditing();
            }
        });
    }

    // Set row numbers in the first column
    public void updateRowHeaders() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(i + 1, i, 0);
        }
    }
}
