package com.deepsleep.app;

import com.deepsleep.api.enums.UserRole;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

public enum ViewRoute {
    ACCOUNT("账号设置", "通用", "/com/deepsleep/ui/common/account-settings-view.fxml", roles()),
    NOTICE_LIST("公告列表", "通用", "/com/deepsleep/ui/common/notice-list-view.fxml", roles()),
    EXAM_DETAIL("考试详情", "通用", "/com/deepsleep/ui/common/exam-detail-view.fxml", roles()),

    ADMIN_USERS("用户管理", "管理员", "/com/deepsleep/ui/admin/admin-users-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_STUDENT_FORM("学生表单", "管理员", "/com/deepsleep/ui/admin/admin-student-form.fxml", roles(UserRole.ADMIN)),
    ADMIN_TEACHER_FORM("教师表单", "管理员", "/com/deepsleep/ui/admin/admin-teacher-form.fxml", roles(UserRole.ADMIN)),
    ADMIN_USER_EDIT("账号编辑", "管理员", "/com/deepsleep/ui/admin/admin-user-edit-form.fxml", roles(UserRole.ADMIN)),
    ADMIN_ORGANIZATION("组织架构", "管理员", "/com/deepsleep/ui/admin/admin-organization-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_CLASSROOMS("教室管理", "管理员", "/com/deepsleep/ui/admin/admin-classrooms-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_COURSES("课程与排课管理", "管理员", "/com/deepsleep/ui/admin/admin-course-management-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_EXAMS("考试管理", "管理员", "/com/deepsleep/ui/admin/admin-exams-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_NOTICES("公告管理", "管理员", "/com/deepsleep/ui/admin/admin-notices-view.fxml", roles(UserRole.ADMIN)),
    ADMIN_LOGS("操作日志", "管理员", "/com/deepsleep/ui/admin/admin-logs-view.fxml", roles(UserRole.ADMIN)),

    TEACHER_PROFILE("教师资料", "教师", "/com/deepsleep/ui/teacher/teacher-profile-view.fxml", roles(UserRole.TEACHER)),
    TEACHER_COURSES("教学工作台", "教师", "/com/deepsleep/ui/teacher/teacher-teaching-workspace-view.fxml", roles(UserRole.TEACHER)),

    STUDENT_PROFILE("学生资料", "学生", "/com/deepsleep/ui/student/student-profile-view.fxml", roles(UserRole.STUDENT)),
    STUDENT_COURSES("我的课程", "学生", "/com/deepsleep/ui/student/student-courses-view.fxml", roles(UserRole.STUDENT)),
    STUDENT_ANALYSIS("成绩分析", "学生", "/com/deepsleep/ui/student/student-analysis-view.fxml", roles(UserRole.STUDENT));

    private final String label;
    private final String group;
    private final String resourcePath;
    private final EnumSet<UserRole> roles;

    ViewRoute(String label, String group, String resourcePath, EnumSet<UserRole> roles) {
        this.label = label;
        this.group = group;
        this.resourcePath = resourcePath;
        this.roles = roles;
    }

    public String label() {
        return label;
    }

    public String group() {
        return group;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public boolean supports(UserRole role) {
        return roles.contains(role);
    }

    public static List<ViewRoute> forRole(UserRole role) {
        return Stream.of(values())
                .filter(route -> route.supports(role))
                .toList();
    }

    public static ViewRoute defaultFor(UserRole role) {
        return switch (role) {
            case ADMIN -> ADMIN_USERS;
            case TEACHER -> TEACHER_COURSES;
            case STUDENT -> STUDENT_COURSES;
            case UNKNOWN -> NOTICE_LIST;
        };
    }

    private static EnumSet<UserRole> roles(UserRole... roles) {
        if (roles.length == 0) {
            return EnumSet.of(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT);
        }
        return EnumSet.copyOf(List.of(roles));
    }
}
