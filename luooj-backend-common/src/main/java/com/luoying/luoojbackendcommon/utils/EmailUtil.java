package com.luoying.luoojbackendcommon.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import static com.luoying.luoojbackendcommon.constant.EmailConstant.*;


/**
 * 电子邮件模板内容生成工具类
 */
@Slf4j
public class EmailUtil {


    /**
     * 生成电子邮件内容
     *
     * @param captcha       验证码
     * @param emailHtmlPath 电子邮件html模板的额路径
     */
    public static String buildEmailContent(String emailHtmlPath, String captcha) {
        // 加载邮件html模板
        ClassPathResource resource = new ClassPathResource(emailHtmlPath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuilder buffer = new StringBuilder();
        String line;
        // 读取模板
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            log.info("读取邮件模板失败{}", e.getMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 替换html模板中的参数
        return MessageFormat.format(buffer.toString(), captcha, EMAIL_TITLE, EMAIL_TITLE_ENGLISH, PLATFORM_RESPONSIBLE_PERSON, PLATFORM_ADDRESS);
    }
}
