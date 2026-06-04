package com.deepsleep.api.service;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.notice.NoticeRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.PageResult;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.NoticeVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

public class NoticeApi {

    private final ApiClient apiClient;

    public NoticeApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> createNotice(NoticeRequest request) {
        return apiClient.postVoid("/admin/notice", request);
    }

    public CompletableFuture<Void> deleteNotice(Long noticeId) {
        return apiClient.delete("/admin/notice/%d".formatted(noticeId));
    }

    public CompletableFuture<Void> updateNotice(Long noticeId, NoticeRequest request) {
        return apiClient.putVoid("/admin/notice/%d".formatted(noticeId), request);
    }

    public CompletableFuture<PageResult<NoticeVO>> listNotices(PageQuery query) {
        return apiClient.get(
                "/notice/list",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<NoticeVO>>>() {}
        );
    }

    public CompletableFuture<NoticeVO> getNotice(Long noticeId) {
        return apiClient.get("/notice/%d".formatted(noticeId), new TypeReference<Result<NoticeVO>>() {});
    }
}
