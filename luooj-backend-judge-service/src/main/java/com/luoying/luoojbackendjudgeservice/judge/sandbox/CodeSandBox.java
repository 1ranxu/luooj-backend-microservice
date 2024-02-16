package com.luoying.luoojbackendjudgeservice.judge.sandbox;


import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;

/**
 * @author 落樱的悔恨
 * 代码沙箱
 */
public interface CodeSandBox {

    /**
     * 执行代码
     * @param executeCodeRequest 执行代码响应
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
