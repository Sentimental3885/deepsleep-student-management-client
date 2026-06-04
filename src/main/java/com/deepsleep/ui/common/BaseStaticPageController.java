package com.deepsleep.ui.common;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseStaticPageController {

    @FXML
    protected Label pageTitle;
    @FXML
    protected Label pageSubtitle;
    @FXML
    protected Label emptyState;
    @FXML
    protected FlowPane statPane;
    @FXML
    protected TableView<ObservableList<String>> dataTable;
    @FXML
    protected VBox formBox;
    @FXML
    protected TextArea contentArea;
    @FXML
    protected Button refreshButton;
    @FXML
    protected Button createButton;
    @FXML
    protected Button editButton;
    @FXML
    protected Button deleteButton;

    @FXML
    public void initialize() {
        StaticPageSpec spec = pageSpec();
        pageTitle.setText(spec.title());
        pageSubtitle.setText(spec.subtitle());
        fillStats(spec);
        fillTable(spec);
        fillForm(spec);
        fillContent(spec);
        tuneActions(spec);
        configureActions();
        loadRemoteData();
    }

    protected abstract StaticPageSpec pageSpec();

    protected void loadRemoteData() {
    }

    protected void configureActions() {
    }

    @FXML
    public final void onRefresh() {
        handleRefresh();
    }

    protected void handleRefresh() {
        loadRemoteData();
    }

    @FXML
    public final void onCreate() {
        handleCreate();
    }

    protected void handleCreate() {
        showStatus("当前页面未启用该操作。");
    }

    @FXML
    public final void onEdit() {
        handleEdit();
    }

    protected void handleEdit() {
        showStatus("当前页面未启用该操作。");
    }

    @FXML
    public final void onDelete() {
        handleDelete();
    }

    protected void handleDelete() {
        showStatus("当前页面未启用该操作。");
    }

    private void fillStats(StaticPageSpec spec) {
        statPane.getChildren().clear();
        if (spec.stats().isEmpty() || isFormPage(spec)) {
            statPane.setVisible(false);
            statPane.setManaged(false);
            return;
        }
        statPane.setVisible(true);
        statPane.setManaged(true);
        spec.stats().forEach(text -> {
            Label card = new Label(text);
            card.getStyleClass().add("stat-card");
            statPane.getChildren().add(card);
        });
    }

    protected void fillTable(StaticPageSpec spec) {
        dataTable.getColumns().clear();
        if (spec.columns().isEmpty()) {
            dataTable.setVisible(false);
            dataTable.setManaged(false);
            emptyState.setText("请填写表单后执行操作。");
            return;
        }

        dataTable.setVisible(true);
        dataTable.setManaged(true);
        for (int i = 0; i < spec.columns().size(); i++) {
            int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(spec.columns().get(i));
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    data.getValue().size() > columnIndex ? data.getValue().get(columnIndex) : ""
            ));
            column.setPrefWidth(150);
            dataTable.getColumns().add(column);
        }

        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        spec.rows().forEach(row -> items.add(FXCollections.observableArrayList(row)));
        dataTable.setItems(items);
        emptyState.setText(items.isEmpty() ? "暂无数据。" : "数据已加载。");
    }

    private void fillForm(StaticPageSpec spec) {
        formBox.getChildren().clear();
        if (spec.formFields().isEmpty()) {
            formBox.setVisible(false);
            formBox.setManaged(false);
            return;
        }

        formBox.setVisible(true);
        formBox.setManaged(true);
        spec.formFields().forEach(field -> {
            TextField textField = new TextField();
            textField.setPromptText(field);
            textField.getStyleClass().add("form-input");
            formBox.getChildren().add(textField);
        });
    }

    private void fillContent(StaticPageSpec spec) {
        if (spec.content() == null || spec.content().isBlank()) {
            contentArea.setVisible(false);
            contentArea.setManaged(false);
            return;
        }
        contentArea.setText(spec.content());
    }

    private void tuneActions(StaticPageSpec spec) {
        if (isFormPage(spec)) {
            refreshButton.setText("加载");
            createButton.setText("保存");
            editButton.setText("重置");
            deleteButton.setText("返回");
        }
    }

    private boolean isFormPage(StaticPageSpec spec) {
        return spec.columns().isEmpty() && !spec.formFields().isEmpty();
    }

    protected void setTable(List<String> columns, List<List<String>> rows) {
        fillTable(new StaticPageSpec(pageTitle.getText(), pageSubtitle.getText(), List.of(), columns, rows, List.of(), ""));
    }

    protected void setContent(String value) {
        contentArea.setVisible(true);
        contentArea.setManaged(true);
        contentArea.setText(value == null ? "" : value);
    }

    protected String formValue(String promptText) {
        return formBox.getChildren().stream()
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .filter(field -> promptText.equals(field.getPromptText()))
                .map(TextField::getText)
                .findFirst()
                .orElse("");
    }

    protected Long formLong(String promptText) {
        String value = formValue(promptText);
        return value == null || value.isBlank() ? null : Long.valueOf(value.trim());
    }

    protected Integer formInt(String promptText) {
        String value = formValue(promptText);
        return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
    }

    protected BigDecimal formDecimal(String promptText) {
        String value = formValue(promptText);
        return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
    }

    protected LocalDate formDate(String promptText) {
        String value = formValue(promptText);
        return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
    }

    @SuppressWarnings("SameParameterValue")
    protected LocalDateTime formDateTime(String promptText) {
        String value = formValue(promptText);
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace(' ', 'T');
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected List<Long> formLongList(String promptText) {
        String value = formValue(promptText);
        List<Long> values = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return values;
        }
        for (String item : value.split(",")) {
            if (!item.isBlank()) {
                values.add(Long.valueOf(item.trim()));
            }
        }
        return values;
    }

    protected Long selectedLong(int columnIndex) {
        ObservableList<String> row = dataTable.getSelectionModel().getSelectedItem();
        if (row == null || row.size() <= columnIndex || row.get(columnIndex).isBlank()) {
            return null;
        }
        return Long.valueOf(row.get(columnIndex));
    }

    @SuppressWarnings("SameParameterValue")
    protected String selectedString(int columnIndex) {
        ObservableList<String> row = dataTable.getSelectionModel().getSelectedItem();
        if (row == null || row.size() <= columnIndex) {
            return null;
        }
        return row.get(columnIndex);
    }

    protected void showStatus(String message) {
        emptyState.setText(message);
    }

    protected void hideCreateButton() {
        createButton.setVisible(false);
        createButton.setManaged(false);
    }

    @SuppressWarnings("SameParameterValue")
    protected void setCreateText(String text) {
        createButton.setText(text);
    }

    @SuppressWarnings("SameParameterValue")
    protected void setEditText(String text) {
        editButton.setText(text);
    }

    @SuppressWarnings("SameParameterValue")
    protected void setDeleteText(String text) {
        deleteButton.setText(text);
    }
}
