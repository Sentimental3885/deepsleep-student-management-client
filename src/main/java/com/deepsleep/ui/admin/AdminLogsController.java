package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminLogsController extends BaseStaticPageController {

    private int pageNum = 1;
    private long totalPages = 1;

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminLogs();
    }

    @Override
    protected void configureActions() {
        hideCreateButton();
        setEditText("上一页");
        setDeleteText("下一页");
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listLogs(new PageQuery(pageNum, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    totalPages = page.pages() == null ? 1 : Math.max(1, page.pages());
                    setTable(List.of("操作人", "操作", "方法", "状态", "时间"), page.records().stream().map(Rows::log).toList());
                    showStatus("日志加载完成，第 " + pageNum + " / " + totalPages + " 页，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        if (pageNum <= 1) {
            showStatus("已经是第一页。");
            return;
        }
        pageNum--;
        loadRemoteData();
    }

    @Override
    protected void handleDelete() {
        if (pageNum >= totalPages) {
            showStatus("已经是最后一页。");
            return;
        }
        pageNum++;
        loadRemoteData();
    }
}
