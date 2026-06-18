package com.deepsleep.ui.common;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public final class CourseTables {

    private CourseTables() {
    }

    public static TableView<ObservableList<String>> table(List<String> columns) {
        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        configure(table, columns);
        return table;
    }

    public static void configure(TableView<ObservableList<String>> table, List<String> columns) {
        table.getColumns().clear();
        for (int i = 0; i < columns.size(); i++) {
            int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columns.get(i));
            Label header = new Label(columns.get(i));
            header.setWrapText(true);
            column.setText(null);
            column.setGraphic(header);
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    data.getValue().size() > columnIndex ? data.getValue().get(columnIndex) : ""
            ));
            column.setPrefWidth(120);
            column.setMinWidth(74);
            table.getColumns().add(column);
        }
    }

    public static void setColumnWidths(TableView<ObservableList<String>> table, double... widths) {
        for (int i = 0; i < widths.length && i < table.getColumns().size(); i++) {
            TableColumn<ObservableList<String>, ?> column = table.getColumns().get(i);
            column.setPrefWidth(widths[i]);
            column.setMinWidth(Math.min(widths[i], 90));
        }
    }

    public static void setRows(TableView<ObservableList<String>> table, List<List<String>> rows) {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        rows.forEach(row -> items.add(FXCollections.observableArrayList(row)));
        table.setItems(items);
    }

    public static void makeEditableTextColumn(
            TableView<ObservableList<String>> table,
            int columnIndex,
            Consumer<ObservableList<String>> onEdit
    ) {
        if (table.getColumns().size() <= columnIndex) {
            return;
        }
        table.setEditable(true);
        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>, String> column =
                (TableColumn<ObservableList<String>, String>) table.getColumns().get(columnIndex);
        column.setEditable(true);
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(event -> {
            ObservableList<String> row = event.getRowValue();
            while (row.size() <= columnIndex) {
                row.add("");
            }
            row.set(columnIndex, event.getNewValue() == null ? "" : event.getNewValue().trim());
            table.refresh();
            if (onEdit != null) {
                onEdit.accept(row);
            }
        });
    }

    public static Long selectedLong(TableView<ObservableList<String>> table, int columnIndex) {
        ObservableList<String> row = table.getSelectionModel().getSelectedItem();
        if (row == null || row.size() <= columnIndex || row.get(columnIndex).isBlank()) {
            return null;
        }
        return Long.valueOf(row.get(columnIndex));
    }

    public static String value(TextField field) {
        return field == null ? "" : field.getText();
    }

    public static Integer intValue(TextField field) {
        String value = value(field);
        return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
    }

    public static BigDecimal decimalValue(TextField field) {
        String value = value(field);
        return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
    }
}
