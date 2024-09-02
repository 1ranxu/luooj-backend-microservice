package com.luoying.luoojbackendcommon.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author 落樱的悔恨
 * @Date 2024/3/18 20:22
 */
@Data
public class RedisData {
    private Object data;
    private LocalDateTime expireTime;
}
