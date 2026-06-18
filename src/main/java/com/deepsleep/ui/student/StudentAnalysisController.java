package com.deepsleep.ui.student;

import com.deepsleep.api.vo.GradeAnalysisVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;

import java.util.List;

public class StudentAnalysisController extends BaseStaticPageController {

    private List<GradeAnalysisVO> currentHistory = List.of();

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentAnalysis();
    }

    @Override
    protected void configureActions() {
        editButton.setText("查看详情");
        contentArea.clear();
        contentArea.setVisible(false);
        contentArea.setManaged(false);
        dataTable.setRowFactory(table -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty() || event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 2) {
                    return;
                }
                showAnalysisAt(row.getIndex());
            });
            return row;
        });
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().analysisApi().listMyAnalysisHistory()
                .whenComplete(UiAsync.onComplete(history -> {
                    currentHistory = history == null ? List.of() : history;
                    setTable(List.of("生成时间", "内容摘要"), currentHistory.stream().map(this::analysisRow).toList());
                    tuneHistoryColumns();
                    showStatus("分析历史加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().analysisApi().analyzeMyGrades()
                .whenComplete(UiAsync.onComplete(analysis -> {
                    showAnalysisDialog(analysis);
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        showAnalysisAt(dataTable.getSelectionModel().getSelectedIndex());
    }

    private void showAnalysisAt(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= currentHistory.size()) {
            showStatus("请先选择一条历史分析记录。");
            return;
        }
        showAnalysisDialog(currentHistory.get(selectedIndex));
        showStatus("已显示选中的历史分析。");
    }

    private void showAnalysisDialog(GradeAnalysisVO analysis) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("成绩分析详情");
        dialog.setHeaderText("生成时间：" + Rows.text(analysis.createTime()));
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(820);

        TextArea detailArea = new TextArea(text(analysis.content()));
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.setPrefSize(760, 520);
        detailArea.setMinHeight(420);
        dialog.getDialogPane().setContent(detailArea);
        dialog.showAndWait();
    }

    private List<String> analysisRow(GradeAnalysisVO analysis) {
        return List.of(Rows.text(analysis.createTime()), summary(analysis.content()));
    }

    private void tuneHistoryColumns() {
        if (dataTable.getColumns().size() < 2) {
            return;
        }
        dataTable.getColumns().get(0).setPrefWidth(180);
        dataTable.getColumns().get(1).setPrefWidth(760);
    }

    private String summary(String content) {
        String text = text(content).replaceAll("\\s+", " ").trim();
        int maxLength = 80;
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
