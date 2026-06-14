package com.deepsleep.api.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    INVALID_USERNAME_OR_PASSWORD(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    USER_NOT_FOUND(1004, "用户不存在"),
    PHONE_OCCUPIED(1005, "手机号已被占用"),
    EMAIL_OCCUPIED(1006, "邮箱已被占用"),
    EMAIL_NOT_BOUND(1007, "当前账号未绑定邮箱"),

    STUDENT_EXISTS(1101, "学生已存在"),
    TEACHER_EXISTS(1102, "教师已存在"),

    STUDENT_NOT_FOUND(2000, "学生不存在"),
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_UNAVAILABLE(2002, "课程不可选"),
    COURSE_FULL(2003, "课程已满员"),

    SELECTION_NOT_FOUND(2100, "课程未被选"),
    COURSE_PICKED(2102, "课程已被选"),
    COURSE_DROPPED(2103, "课程已退选"),
    COURSE_OVER(2104, "课程已修完"),

    NOT_COURSE_TEACHER(2200, "非授课教师无权更改"),
    INVALID_SCORE(2201, "分数非法"),
    SCORE_NOT_PUBLISHED(2202, "成绩未公布"),

    TEACHER_NOT_FOUND(3000, "教师不存在"),
    CLASS_NOT_FOUND(3001, "班级不存在"),
    COURSE_EXISTS(3002, "课程已存在"),
    INVALID_COURSE_STATUS(3003, "课程状态码非法"),
    CAPACITY_TOO_SMALL(3004, "所设容量过小"),

    SCHEDULE_NOT_FOUND(3100, "课程不存在"),
    SCHEDULE_COURSE_MISMATCH(3101, "排课与课程不匹配"),
    SCHEDULE_CONFLICT(3102, "排课冲突"),

    CLASSROOM_NOT_FOUND(4000, "教室不存在"),
    CLASSROOM_CONFLICT(4001, "教室名称已存在"),
    CLASSROOM_HAS_REFERENCES(4002, "教室被排课或考试使用中，无法删除"),

    DEPT_CONFLICT(5001, "学院名称已存在"),
    DEPT_HAS_REFERENCES(5002, "学院下还有专业/学生/教师，无法删除"),
    MAJOR_CONFLICT(5003, "该学院下专业名称已存在"),
    MAJOR_HAS_REFERENCES(5004, "专业下还有班级/学生，无法删除"),
    CLAZZ_CONFLICT(5005, "班级名称已存在"),
    CLAZZ_HAS_REFERENCES(5006, "班级下还有学生，无法删除"),

    AI_SERVICE_ERROR(6000, "AI服务异常"),
    NO_GRADES_TO_ANALYZE(6001, "暂无成绩可供分析"),

    FILE_UPLOAD_FAILED(7000, "文件上传失败"),
    FILE_DELETE_FAILED(7001, "文件删除失败"),
    FILE_EMPTY(7002, "文件为空"),
    FILE_UNSUPPORTED(7003, "文件格式不支持"),
    FILE_TOO_LARGE(7004, "请求/请求文件体积过大"),

    EMAIL_CODE_EXPIRED(8000, "邮箱验证码不存在或已过期"),
    EMAIL_CODE_ERROR(8001, "邮箱验证码错误"),
    EMAIL_SEND_FAILED(8003, "构建/发送HTML邮件失败"),
    SMS_SEND_FAILED(8004, "短信发送失败"),

    CODE_NOT_EXISTS(9000, "还未对目标发送验证码"),
    CODE_SEND_TOO_FREQUENTLY(9001, "对同一目标的验证码发送过于频繁"),
    CODE_INCORRECT(9002, "验证码错误"),
    CODE_FAILED_ATTEMPTS_TOO_MUCH(9003, "验证码校验失败次数过多"),

    UNKNOWN(-1, "未知业务错误"),

    ;

    private final Integer code;
    private final String msg;

    public static ResultCode of(Integer code) {
        return Arrays.stream(values())
                .filter(resultCode -> Objects.equals(resultCode.code, code))
                .findAny()
                .orElse(UNKNOWN);
    }

    public boolean matches(Integer code) {
        return Objects.equals(this.code, code);
    }
}
