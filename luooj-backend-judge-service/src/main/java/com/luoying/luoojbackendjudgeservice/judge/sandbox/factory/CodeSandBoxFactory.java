package com.luoying.luoojbackendjudgeservice.judge.sandbox.factory;

import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBox;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.impl.RemoteCodeSandBox;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.impl.ThirdPartyCodeSandBox;

/**
 * @author 落樱的悔恨
 * 代码沙箱工厂（根据用户传入的字符串参数（沙箱类别），来生成对应的代码沙箱实现类）
 */
public class CodeSandBoxFactory {

    /**
     * 创建代码沙箱示例
     *
     * @param type 代码沙箱类型
     */
    public static CodeSandBox newInstance(String type) {
        switch (type) {
            case "remote":
                return new RemoteCodeSandBox();
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            default:
                return new ThirdPartyCodeSandBox();
        }
    }
}
