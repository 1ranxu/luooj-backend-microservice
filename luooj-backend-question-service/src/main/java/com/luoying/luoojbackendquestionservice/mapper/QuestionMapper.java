package com.luoying.luoojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luoying.luoojbackendmodel.entity.Question;
import org.apache.ibatis.annotations.Param;


/**
 * @author 落樱的悔恨
 * @description 针对表【question(题目)】的数据库操作Mapper
 * @createDate 2023-11-09 16:32:34
 * @Entity com.luoying.model.entity.Question
 */
public interface QuestionMapper extends BaseMapper<Question> {
    /**
     * 查询上一道题目
     *
     * @param tableName  表名
     * @param questionId 当前题目id
     */
    long getPrevQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

    /**
     * 查询下一道题目
     *
     * @param tableName  表名
     * @param questionId 当前题目id
     */
    long getNextQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

}




