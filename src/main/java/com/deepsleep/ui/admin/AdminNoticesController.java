package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.notice.NoticeRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminNoticesController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminNotices();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().noticeApi()
                .listNotices(new PageQuery(1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "标题", "发布人", "创建时间", "更新时间"),
                            page.records().stream().map(Rows::notice).toList());
                    showStatus("公告加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().noticeApi()
                .createNotice(new NoticeRequest(formValue("公告标题"), formValue("公告正文")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("公告已发布。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long id = selectedLong(0);
        if (id == null) {
            showStatus("请先在表格中选择要编辑的公告。");
            return;
        }
        AppContext.getInstance().apiServices().noticeApi()
                .updateNotice(id, new NoticeRequest(formValue("公告标题"), formValue("公告正文")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("公告已更新。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long id = selectedLong(0);
        if (id == null) {
            showStatus("请先选择公告。");
            return;
        }
        AppContext.getInstance().apiServices().noticeApi()
                .deleteNotice(id)
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("公告已删除。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
