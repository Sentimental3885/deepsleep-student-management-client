package com.deepsleep.api.service;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.selection.EndSelectionRequest;
import com.deepsleep.api.dto.selection.ScoreListQuery;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.PageResult;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.CourseStudentVO;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.ScoreVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectionApi {

    private final ApiClient apiClient;

    public SelectionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<PageResult<CourseVO>> listAvailableCourses(PageQuery query) {
        return apiClient.get(
                "/selection/courseList",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<CourseVO>>>() {}
        );
    }

    public CompletableFuture<Void> pickCourse(Long courseId) {
        return apiClient.postVoid("/selection/pick/%d".formatted(courseId), null);
    }

    public CompletableFuture<Void> dropCourse(Long courseId) {
        return apiClient.delete("/selection/drop/%d".formatted(courseId));
    }

    public CompletableFuture<Void> endCourse(EndSelectionRequest request) {
        return apiClient.putVoid("/selection/end", request);
    }

    public CompletableFuture<PageResult<CourseVO>> listSelections(PageQuery query) {
        return apiClient.get(
                "/selection/selectionList",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<CourseVO>>>() {}
        );
    }

    public CompletableFuture<List<CourseStudentVO>> listCourseStudents(Long courseId) {
        return apiClient.post(
                "/selection/courseStudents/%d".formatted(courseId),
                null,
                new TypeReference<Result<List<CourseStudentVO>>>() {}
        );
    }

    public CompletableFuture<PageResult<ScoreVO>> listScores(ScoreListQuery query) {
        return apiClient.get(
                "/selection/score/list",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<ScoreVO>>>() {}
        );
    }

    public CompletableFuture<ScoreVO> getScore(Long courseId) {
        return apiClient.get("/selection/score/%d".formatted(courseId), new TypeReference<Result<ScoreVO>>() {});
    }
}
