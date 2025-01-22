package com.luoying.luoojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luoying.luoojbackendmodel.entity.Question;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


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
    Long getPrevQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

    /**
     * 查询下一道题目
     *
     * @param tableName  表名
     * @param questionId 当前题目id
     */
    Long getNextQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

    /**
     * 返回第一条记录
     *
     */
    @Select("select id from question order by id asc limit 1")
    Long getFirstQuestion();

    /**
     * 返回最后一条记录
     *
     */
    @Select("select id from question order by id desc limit 1")
    Long getLastQuestion();

}




