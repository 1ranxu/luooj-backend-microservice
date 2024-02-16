package com.luoying.luoojbackendmodel.codesanbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 落樱的悔恨
 * 在线运行代码请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {
    /**
     * 输入用例
     */
    private String input;

    /**
     * 代码
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;
}
