package com.deepsleep.api.vo;

import java.time.LocalDateTime;

public record NoticeVO(
        Long id,
        String title,
        String content,
        Long publisherId,
        String publisherName,
        String publisherAvatar,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
