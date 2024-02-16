package com.luoying.luoojbackendjudgeservice.judge.sandbox.impl;


import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBox;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 落樱的悔恨
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
@Slf4j
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("第三方代码沙箱");
        return null;
    }
}
