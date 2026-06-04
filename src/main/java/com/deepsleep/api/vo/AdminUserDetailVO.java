package com.deepsleep.api.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserDetailVO(
        Long id,
        String username,
        String name,
        String phone,
        String email,
        String avatar,
        Integer gender,
        Integer role,
        LocalDateTime createTime,
        StudentInfo studentInfo,
        TeacherInfo teacherInfo
) {

    public record StudentInfo(
            Long deptId,
            String deptName,
            Long majorId,
            String majorName,
            Long clazzId,
            String clazzName,
            String position,
            LocalDate entryDate
    ) {
    }

    public record TeacherInfo(
            Long deptId,
            String deptName,
            String title,
            LocalDate entryDate
    ) {
    }
}
