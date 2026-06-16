package com.deepsleep.ui.common;

import java.util.List;

public final class StaticPageData {

    private StaticPageData() {
    }

    public static StaticPageSpec noticeList() {
        return table("公告列表", "查看系统公告，双击公告可查看正文。",
                List.of(),
                List.of("标题", "发布人", "发布时间", "更新时间"),
                List.of(
                        List.of("期末考试安排说明", "教务处", "2026-06-01 09:00", "2026-06-01 09:00"),
                        List.of("选课系统开放通知", "管理员", "2026-05-28 14:30", "2026-05-29 08:10")
                ));
    }

    public static StaticPageSpec courseDetail() {
        return spec("课程详情", "展示课程基础信息与排课列表；管理员和授课教师可进入排课维护。",
                List.of(),
                List.of("周次", "节次", "教室", "教师"),
                List.of(
                        List.of("1-16 周", "周一 1-2 节", "A101", "张老师"),
                        List.of("1-16 周", "周三 5-6 节", "B203", "张老师")
                ),
                List.of("课程 ID"),
                "");
    }

    public static StaticPageSpec adminUsers() {
        return table("用户管理", "管理员维护学生、教师账号，支持筛选、分页、详情、重置密码和删除。",
                List.of("用户 1280", "学生 1130", "教师 150"),
                List.of("用户名", "姓名", "角色", "手机号", "邮箱", "创建时间"),
                List.of(
                        List.of("2024001001", "林一", "学生", "13800000001", "linyi@example.com", "2026-03-01"),
                        List.of("T2024012", "陈老师", "教师", "13900000012", "chen@example.com", "2026-02-18")
                ));
    }

    public static StaticPageSpec adminOrganization() {
        return table("组织架构", "维护学院、专业、班级三级数据，删除前需处理引用关系。",
                List.of("学院 8", "专业 32", "班级 126"),
                List.of("ID", "层级", "名称", "上级", "年级"),
                List.of(
                        List.of("学院", "计算机学院", "-", "-"),
                        List.of("专业", "软件工程", "计算机学院", "-"),
                        List.of("班级", "软工 2401", "软件工程", "2024")
                ),
                List.of("类型", "ID", "名称", "学院 ID", "专业 ID", "年级"));
    }

    public static StaticPageSpec adminClassrooms() {
        return table("教室管理", "维护教室名称，删除前后端会检查排课和考试引用。",
                List.of("教室 64", "可用 58", "冲突校验"),
                List.of("教室名称"),
                List.of(
                        List.of("A101"),
                        List.of("B203")
                ));
    }

    public static StaticPageSpec adminCourses() {
        return table("课程管理", "管理员创建课程、维护容量状态和适用班级，并进入排课管理。",
                List.of("课程 96", "开课 84", "未开课 12"),
                List.of("ID", "课程代码", "课程名称", "教师", "学期", "容量", "状态"),
                List.of(
                        List.of("CS101", "数据结构", "张老师", "2025-2026-2", "60", "开课"),
                        List.of("MA201", "线性代数", "王老师", "2025-2026-2", "80", "开课")
                ),
                List.of("课程 ID", "课程名称", "教师 ID", "课程代码", "学期", "容量", "学分", "状态", "课程简介", "适用班级 IDs"));
    }

    public static StaticPageSpec adminExams() {
        return table("考试管理", "管理员分页查看考试，创建或调整考试安排。",
                List.of("考试 18", "本月 6", "冲突校验"),
                List.of("课程", "类型", "时间", "时长", "教室", "监考教师", "备注"),
                List.of(
                        List.of("数据结构", "期末", "2026-07-01 09:00", "A301", "李老师"),
                        List.of("线性代数", "期中", "2026-06-18 14:00", "B102", "赵老师")
                ));
    }

    public static StaticPageSpec adminNotices() {
        return table("公告管理", "管理员发布、编辑和删除公告，普通用户从公告列表阅读。",
                List.of(),
                List.of("标题", "发布人", "创建时间", "更新时间"),
                List.of(List.of("选课系统开放通知", "管理员", "2026-05-28 14:30", "2026-05-29 08:10")));
    }

    public static StaticPageSpec adminLogs() {
        return table("操作日志", "分页查看管理员操作日志，按创建时间倒序。",
                List.of("今日 24", "失败 1", "分页 /admin/log/list"),
                List.of("操作人", "操作", "方法", "状态", "时间"),
                List.of(
                        List.of("管理员", "新增课程", "POST", "成功", "2026-06-04 09:12"),
                        List.of("管理员", "删除教室", "DELETE", "失败", "2026-06-04 10:03")
                ));
    }

    public static StaticPageSpec teacherProfile() {
        return spec("教师资料", "教师查看并维护职称、入职日期等个人扩展资料。",
                List.of(),
                List.of(),
                List.of(),
                List.of("职称", "入职日期"),
                "");
    }

    public static StaticPageSpec teacherCourses() {
        return table("我的课程", "教师查看授课课程，并进入课程维护、排课和学生名单。",
                List.of("授课 4", "开课 3", "待结课 2"),
                List.of("ID", "课程代码", "课程名称", "学期", "学分", "容量", "已选", "状态"),
                List.of(
                        List.of("CS101", "数据结构", "2025-2026-2", "60", "42", "开课"),
                        List.of("CS205", "Java 程序设计", "2025-2026-2", "50", "48", "开课")
                ));
    }

    public static StaticPageSpec teacherCourseStudents() {
        return table("课程学生", "教师查看选课学生，录入分数并结束课程。",
                List.of("学生 42", "已评分 28", "未评分 14"),
                List.of("学生ID", "学号", "姓名", "选课状态", "成绩"),
                List.of(
                        List.of("2024001001", "林一", "PICKED", "92.5"),
                        List.of("2024001002", "周二", "PICKED", "待录入")
                ),
                List.of("课程 ID", "学生 ID", "成绩"));
    }

    public static StaticPageSpec teacherExams() {
        return table("监考安排", "教师查看自己负责监考的考试列表。",
                List.of("待监考 3", "本周 1", "按时间升序"),
                List.of("课程", "类型", "时间", "教室", "备注"),
                List.of(List.of("数据结构", "期末", "2026-07-01 09:00", "A301", "闭卷")));
    }

    public static StaticPageSpec studentProfile() {
        return spec("学生资料", "学生查看学院、专业、班级和职务信息，可维护部分个人扩展资料。",
                List.of(),
                List.of(),
                List.of(),
                List.of("班级", "职务", "入学日期"),
                "");
    }

    public static StaticPageSpec studentSchedule() {
        return table("我的课表", "基于未退选课程展示学生课表，后续可切换周视图。",
                List.of("本周课程 8", "教室 5", "教师 4"),
                List.of("课程ID", "课程", "星期", "节次", "周次", "教室"),
                List.of(
                        List.of("周一", "1-2 节", "数据结构", "A101", "1-16 周"),
                        List.of("周三", "5-6 节", "大学英语", "C204", "1-16 周")
                ));
    }

    public static StaticPageSpec studentExams() {
        return table("我的考试", "展示当前学生未退选课程关联的考试，按考试时间升序。",
                List.of("待考试 3", "最近 1 场", "按时间升序"),
                List.of("课程", "类型", "时间", "教室", "时长"),
                List.of(List.of("数据结构", "期末", "2026-07-01 09:00", "A301", "120 分钟")));
    }

    public static StaticPageSpec studentCourseSelection() {
        return table("选课中心", "展示当前班级可选且课程状态为开课的课程，支持选课。",
                List.of("可选 12", "容量紧张 3", "分页 /selection/courseList"),
                List.of("ID", "课程代码", "课程名称", "教师", "学期", "学分", "容量", "已选", "状态"),
                List.of(
                        List.of("CS101", "数据结构", "张老师", "3.0", "60", "42"),
                        List.of("PE102", "羽毛球", "刘老师", "1.0", "30", "29")
                ));
    }

    public static StaticPageSpec studentSelectedCourses() {
        return table("已选课程", "展示学生已选课程并支持退课。",
                List.of("已选 6", "总学分 16.5", "可退 5"),
                List.of("ID", "课程代码", "课程名称", "教师", "学期", "学分", "容量", "已选", "状态"),
                List.of(
                        List.of("CS101", "数据结构", "张老师", "2025-2026-2", "3.0", "PICKED"),
                        List.of("EN201", "大学英语", "孙老师", "2025-2026-2", "2.0", "PICKED")
                ));
    }

    public static StaticPageSpec studentScores() {
        return table("成绩查询", "按学期分页查询成绩，展示 GPA、排名和最高最低分。",
                List.of("平均绩点 3.62", "已出分 5", "待出分 1"),
                List.of("ID", "课程代码", "课程名称", "学期", "学分", "成绩", "GPA", "排名", "最高分", "最低分"),
                List.of(
                        List.of("CS101", "数据结构", "92.5", "4.0", "3/42", "98", "61"),
                        List.of("EN201", "大学英语", "88.0", "3.7", "8/120", "96", "55")
                ),
                List.of("学期"));
    }

    public static StaticPageSpec studentAnalysis() {
        return spec("成绩分析", "生成当前学生成绩分析，并展示历史分析记录。",
                List.of("分析历史 3", "最近生成 2026-06-02", "AI 服务占位"),
                List.of("生成时间", "摘要"),
                List.of(
                        List.of("2026-06-02 20:10", "专业课表现稳定，英语成绩有提升空间。"),
                        List.of("2026-05-20 18:40", "整体绩点较上月提升。")
                ),
                List.of(),
                "这里展示 AI 成绩分析正文占位。后续调用 AnalysisApi.analyzeMyGrades 和 listMyAnalysisHistory 后，将左侧历史与右侧正文拆分展示。");
    }

    public static StaticPageSpec courseForm() {
        return spec("课程表单", "新增或编辑课程基础信息，适用班级后续使用多选控件。",
                List.of(),
                List.of(),
                List.of(),
                List.of("课程 ID", "课程名称", "教师 ID", "课程代码", "学期", "容量", "学分", "状态", "课程简介", "适用班级 IDs"),
                "");
    }

    public static StaticPageSpec scheduleForm() {
        return spec("排课表单", "维护课程排课，后续接入教室列表和冲突校验提示。",
                List.of(),
                List.of(),
                List.of(),
                List.of("课程 ID", "排课 ID", "星期", "节次", "开始周", "结束周", "教室"),
                "");
    }

    public static StaticPageSpec examForm() {
        return spec("考试表单", "创建或编辑考试，后端校验时间晚于当前时间。",
                List.of(),
                List.of(),
                List.of(),
                List.of("考试 ID", "课程", "考试类型", "考试时间", "时长", "教室", "监考教师", "备注"),
                "");
    }

    public static StaticPageSpec noticeForm() {
        return spec("公告表单", "发布或编辑公告，标题最长 100 字。",
                List.of(),
                List.of(),
                List.of(),
                List.of("公告标题", "公告正文"),
                "");
    }

    public static StaticPageSpec organizationForm() {
        return spec("组织表单", "新增或编辑学院、专业、班级的小型复用表单。",
                List.of(),
                List.of(),
                List.of(),
                List.of("类型", "ID", "名称", "所属学院", "所属专业", "年级"),
                "");
    }

    public static StaticPageSpec classroomForm() {
        return spec("教室表单", "新增或编辑教室名称。",
                List.of(),
                List.of(),
                List.of(),
                List.of("教室 ID", "教室名称"),
                "");
    }

    private static StaticPageSpec table(String title, String subtitle, List<String> stats, List<String> columns, List<List<String>> rows) {
        return spec(title, subtitle, stats, columns, rows, List.of(), "");
    }

    private static StaticPageSpec table(String title, String subtitle, List<String> stats, List<String> columns,
                                        List<List<String>> rows, List<String> formFields) {
        return spec(title, subtitle, stats, columns, rows, formFields, "");
    }

    private static StaticPageSpec spec(String title, String subtitle, List<String> stats, List<String> columns,
                                      List<List<String>> rows, List<String> formFields, String content) {
        return new StaticPageSpec(title, subtitle, stats, columns, rows, formFields, content);
    }
}
