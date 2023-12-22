package com.luoying.luoojbackendquestionservice.mapper;

import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

@SpringBootTest
@Slf4j
class QuestionSubmitMapperTest {
    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Test
    void addQuestionSubmit() {
        long userId = 1722221123978498050L;
        String tableName = "question_submit_" + userId;

        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setLanguage("java");
        questionSubmit.setQuestionId(1722617540408606721L);
        questionSubmit.setUserId(userId);
        questionSubmit.setCode("import java.util.Scanner;\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner scanner = new Scanner(System.in);\n" +
                "        int a = scanner.nextInt();\n" +
                "        int b = scanner.nextInt();\n" +
                "        System.out.println(a + b);\n" +
                "    }\n" +
                "}");
        questionSubmit.setJudgeInfo("{\"message\":\"Accepted\",\"memory\":500,\"time\":164}");
        questionSubmit.setStatus(2);
        int resultData = questionSubmitMapper.addQuestionSubmit(tableName, questionSubmit);
        System.out.println(resultData);
    }

    @Test
    void queryAcceptedQuestionList() {
        long userId = 1722221123978498050L;
        String tableName = "question_submit_" + userId;

        List<QuestionSubmit> resultData = questionSubmitMapper.queryQuestionSubmitList(tableName);
        log.info("个人提交记录{}", resultData);
    }

    @Test
    void updateQuestionSubmit() {
        long userId = 1722494418820870145L;
        String tableName = "question_submit_" + userId;

        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setId(1738033777324089346L);
        questionSubmit.setStatus(2);
        questionSubmit.setJudgeInfo("{\"message\":\"Accepted\",\"memory\":500,\"time\":86}");

        int resultData = questionSubmitMapper.updateQuestionSubmit(tableName, questionSubmit);
        log.info("个人提交记录{}", resultData);
    }

    @Test
    void existAcceptedQuestionTable() {
        long userId = 1722221123978498050L;
        String tableName = "question_submit_" + userId;
        int result = questionSubmitMapper.existQuestionSubmitTable(tableName);
        log.info("表是否存在{}", result);
    }

    @Test
    void dropAcceptedQuestionTable() {
        long userId = 1722221123978498060L;
        String tableName = "question_submit_" + userId;
        int result = questionSubmitMapper.dropQuestionSubmitTable(tableName);
    }

    @Test
    void createAcceptedQuestionByUserId() {
        long userId = 1722494418820870160L;
        String tableName = "question_submit_" + userId;
        int result = questionSubmitMapper.createQuestionSubmitTable(tableName);
    }
}