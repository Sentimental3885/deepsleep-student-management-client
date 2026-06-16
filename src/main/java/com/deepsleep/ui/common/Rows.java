package com.deepsleep.ui.common;

import com.deepsleep.api.enums.CourseStatus;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Rows {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Rows() {
    }

    public static String text(Object value) {
        return switch (value) {
            case null -> "";
            case LocalDateTime dateTime -> DATE_TIME.format(dateTime);
            case LocalDate date -> date.toString();
            default -> String.valueOf(value);
        };
    }

    public static List<String> adminUser(AdminUserVO user) {
        return List.of(
                text(user.username()),
                text(user.name()),
                UserRole.of(user.role()).label(),
                text(user.phone()),
                text(user.email()),
                text(user.createTime())
        );
    }

    public static List<String> notice(NoticeVO notice) {
        return List.of(
                text(notice.id()),
                text(notice.title()),
                text(notice.publisherName()),
                text(notice.createTime()),
                text(notice.updateTime())
        );
    }

    public static List<String> noticeSummary(NoticeVO notice) {
        return List.of(
                text(notice.title()),
                text(notice.publisherName()),
                text(notice.createTime()),
                text(notice.updateTime())
        );
    }

    public static List<String> log(OperationLogVO log) {
        return List.of(
                text(log.operatorName()),
                text(log.operation()),
                text(log.method()),
                text(log.status()),
                text(log.createTime())
        );
    }

    public static List<String> dept(DeptVO dept) {
        return List.of(text(dept.id()), "学院", text(dept.name()), "", "");
    }

    public static List<String> major(MajorVO major) {
        return List.of(text(major.id()), "专业", text(major.name()), text(major.deptName()), "");
    }

    public static List<String> clazz(ClazzVO clazz) {
        return List.of(text(clazz.id()), "班级", text(clazz.name()), text(clazz.majorName()), text(clazz.grade()));
    }

    public static List<String> classroom(ClassroomVO classroom) {
        return List.of(text(classroom.id()), text(classroom.name()));
    }

    public static List<String> classroomSummary(ClassroomVO classroom) {
        return List.of(text(classroom.name()));
    }

    public static List<String> course(CourseVO course) {
        return List.of(
                text(course.id()),
                text(course.code()),
                text(course.name()),
                text(course.teacherName()),
                text(course.semester()),
                text(course.credit()),
                text(course.capacity()),
                text(course.size()),
                CourseStatus.of(course.status()).label()
        );
    }

    public static List<String> teacherCourse(TeacherCourseVO course) {
        return List.of(
                text(course.id()),
                text(course.code()),
                text(course.name()),
                text(course.semester()),
                text(course.credit()),
                text(course.capacity()),
                text(course.size()),
                CourseStatus.of(course.status()).label()
        );
    }

    public static List<String> schedule(ScheduleVO schedule) {
        return List.of(
                text(schedule.courseId()),
                text(schedule.courseName()),
                "周" + text(schedule.weekday()),
                text(schedule.section()),
                text(schedule.startWeek()) + "-" + text(schedule.endWeek()),
                text(schedule.classroomName())
        );
    }

    public static List<String> exam(ExamVO exam) {
        return List.of(
                text(exam.id()),
                text(exam.courseName()),
                text(exam.typeName()),
                text(exam.examTime()),
                text(exam.duration()),
                text(exam.classroomName()),
                text(exam.invigilatorName()),
                text(exam.remark())
        );
    }

    public static List<String> courseStudent(CourseStudentVO student) {
        return List.of(
                text(student.studentId()),
                text(student.username()),
                text(student.studentName()),
                text(student.selectionStatus()),
                text(student.score())
        );
    }

    public static List<String> score(ScoreVO score) {
        return List.of(
                text(score.id()),
                text(score.code()),
                text(score.name()),
                text(score.semester()),
                text(score.credit()),
                text(score.score()),
                text(score.gpa()),
                text(score.rank()),
                text(score.maxScore()),
                text(score.minScore())
        );
    }

    public static List<String> analysis(GradeAnalysisVO analysis) {
        return List.of(text(analysis.id()), text(analysis.createTime()), text(analysis.content()));
    }
}
