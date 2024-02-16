package com.luoying.luoojbackendquestionservice.mapper;

import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

/**
 * @author 落樱的悔恨
 */
@SpringBootTest
@Slf4j
class AcceptedQuestionMapperTest {
    @Resource
    private AcceptedQuestionMapper acceptedQuestionMapper;

    @Test
    void addAcceptedQuestion() {
        long userId = 1722221123978498050L;
        String tableName = "accepted_question_" + userId;
        long questionId = 1722617540408606721L;
        int resultData = acceptedQuestionMapper.addAcceptedQuestion(tableName, questionId);
        System.out.println(resultData);
    }

    @Test
    void queryAcceptedQuestionList() {
        long userId = 1722221123978498050L;
        String tableName = "accepted_question_" + userId;
        List<AcceptedQuestion> resultData = acceptedQuestionMapper.queryAcceptedQuestionList(tableName);
        log.info("通过的题目{}", resultData);
    }

    @Test
    void existAcceptedQuestionTable() {
        long userId = 1722221123978498050L;
        String tableName = "accepted_question_" + userId;
        int result = acceptedQuestionMapper.existAcceptedQuestionTable(tableName);
        log.info("表是否存在{}", result);
    }
    @Test
    void dropAcceptedQuestionTable() {
        long userId = 1722494418820870168L;
        String tableName = "accepted_question_" + userId;
        int result = acceptedQuestionMapper.dropAcceptedQuestionTable(tableName);
        System.out.println(result);
    }

    @Test
    void createAcceptedQuestionByUserId() {
        long userId = 1722494418820870168L;
        String tableName = "accepted_question_" + userId;
        int result = acceptedQuestionMapper.createAcceptedQuestionTable(tableName);
        System.out.println(result);
    }
}