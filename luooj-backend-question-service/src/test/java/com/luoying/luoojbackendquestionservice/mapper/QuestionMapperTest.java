package com.luoying.luoojbackendquestionservice.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 落樱的悔恨
 */
@SpringBootTest
@Slf4j
class QuestionMapperTest {
    @Resource
    private QuestionMapper questionMapper;

    @Test
    void getPrevQuestion() {
        long questionId = 1733778146547281921L;
        String tableName = "question";
        long result = questionMapper.getPrevQuestion(tableName, questionId);
        log.info("{}",result);
    }

    @Test
    void getNextQuestion() {
        long questionId = 1733778146547281921L;
        String tableName = "question";
        long result = questionMapper.getNextQuestion(tableName, questionId);
        log.info("{}",result);
    }
}