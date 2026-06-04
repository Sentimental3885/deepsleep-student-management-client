package com.deepsleep.ui.common;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.vo.NoticeVO;
import com.deepsleep.app.AppContext;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;

import java.util.List;

public class NoticeListController extends BaseStaticPageController {

    private static final int PAGE_SIZE = 20;

    private List<NoticeVO> currentNotices = List.of();
    private int currentPage = 1;
    private long totalPages = 1;

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.noticeList();
    }

    @Override
    protected void configureActions() {
        setCreateText("查看详情");
        setEditText("上一页");
        setDeleteText("下一页");
        dataTable.setRowFactory(table -> {
            TableRow<javafx.collections.ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    dataTable.getSelectionModel().select(row.getIndex());
                    showSelectedNoticeDetail();
                }
            });
            return row;
        });
    }

    @Override
    protected void loadRemoteData() {
        showStatus("正在加载公告...");
        AppContext.getInstance().apiServices().noticeApi()
                .listNotices(new PageQuery(currentPage, PAGE_SIZE))
                .whenComplete(UiAsync.onComplete(page -> {
                    currentNotices = page.records() == null ? List.of() : page.records();
                    totalPages = page.pages() == null || page.pages() < 1 ? 1 : page.pages();
                    currentPage = page.current() == null ? currentPage : page.current().intValue();
                    setTable(List.of("标题", "发布人", "创建时间", "更新时间"),
                            currentNotices.stream().map(Rows::noticeSummary).toList());
                    showStatus("公告加载完成，第 " + currentPage + " / " + totalPages
                            + " 页，共 " + Rows.text(page.total()) + " 条。双击公告查看详情。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        showSelectedNoticeDetail();
    }

    @Override
    protected void handleEdit() {
        if (currentPage <= 1) {
            showStatus("已经是第一页。");
            return;
        }
        currentPage--;
        loadRemoteData();
    }

    @Override
    protected void handleDelete() {
        if (currentPage >= totalPages) {
            showStatus("已经是最后一页。");
            return;
        }
        currentPage++;
        loadRemoteData();
    }

    private void showSelectedNoticeDetail() {
        NoticeVO selected = selectedNotice();
        if (selected == null || selected.id() == null) {
            showStatus("请先在表格中选择公告。");
            return;
        }
        AppContext.getInstance().apiServices().noticeApi()
                .getNotice(selected.id())
                .whenComplete(UiAsync.onComplete(this::showNoticeDialog, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private NoticeVO selectedNotice() {
        int index = dataTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentNotices.size()) {
            return null;
        }
        return currentNotices.get(index);
    }

    private void showNoticeDialog(NoticeVO notice) {
        TextArea content = new TextArea(Rows.text(notice.content()));
        content.setEditable(false);
        content.setWrapText(true);
        content.setPrefRowCount(14);
        content.setPrefColumnCount(52);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("公告详情");
        alert.setHeaderText(Rows.text(notice.title()));
        alert.getDialogPane().setContent(content);
        if (dataTable.getScene() != null && dataTable.getScene().getWindow() != null) {
            alert.initOwner(dataTable.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
