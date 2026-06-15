package com.deepsleep.api.service;

import com.deepsleep.api.config.ApiConfig;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.auth.TokenStore;

public final class ApiServices {

    private final TokenStore tokenStore;
    private final ApiClient apiClient;
    private final AuthApi authApi;
    private final UserApi userApi;
    private final AdminApi adminApi;
    private final StudentApi studentApi;
    private final TeacherApi teacherApi;
    private final CourseApi courseApi;
    private final SelectionApi selectionApi;
    private final ScheduleApi scheduleApi;
    private final ExamApi examApi;
    private final NoticeApi noticeApi;
    private final OrganizationApi organizationApi;
    private final ClassroomApi classroomApi;
    private final AnalysisApi analysisApi;

    private ApiServices(ApiConfig config, TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        this.apiClient = new ApiClient(config, tokenStore);
        this.authApi = new AuthApi(apiClient, tokenStore);
        this.userApi = new UserApi(apiClient);
        this.adminApi = new AdminApi(apiClient);
        this.studentApi = new StudentApi(apiClient);
        this.teacherApi = new TeacherApi(apiClient);
        this.courseApi = new CourseApi(apiClient);
        this.selectionApi = new SelectionApi(apiClient);
        this.scheduleApi = new ScheduleApi(apiClient);
        this.examApi = new ExamApi(apiClient);
        this.noticeApi = new NoticeApi(apiClient);
        this.organizationApi = new OrganizationApi(apiClient);
        this.classroomApi = new ClassroomApi(apiClient);
        this.analysisApi = new AnalysisApi(apiClient);
    }

    public static ApiServices create(String baseUrl) {
        return create(ApiConfig.of(baseUrl), new TokenStore(null));
    }

    public static ApiServices create(ApiConfig config, TokenStore tokenStore) {
        return new ApiServices(config, tokenStore);
    }

    @SuppressWarnings("unused")
    public TokenStore tokenStore() {
        return tokenStore;
    }

    public ApiClient apiClient() {
        return apiClient;
    }

    public AuthApi authApi() {
        return authApi;
    }

    public UserApi userApi() {
        return userApi;
    }

    public AdminApi adminApi() {
        return adminApi;
    }

    public StudentApi studentApi() {
        return studentApi;
    }

    public TeacherApi teacherApi() {
        return teacherApi;
    }

    public CourseApi courseApi() {
        return courseApi;
    }

    public SelectionApi selectionApi() {
        return selectionApi;
    }

    public ScheduleApi scheduleApi() {
        return scheduleApi;
    }

    public ExamApi examApi() {
        return examApi;
    }

    public NoticeApi noticeApi() {
        return noticeApi;
    }

    public OrganizationApi organizationApi() {
        return organizationApi;
    }

    public ClassroomApi classroomApi() {
        return classroomApi;
    }

    public AnalysisApi analysisApi() {
        return analysisApi;
    }
}
