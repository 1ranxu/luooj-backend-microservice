package com.luoying.luoojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author 落樱的悔恨
 * @description 针对表【accepted_question_userId(题目)】的数据库操作Mapper
 * @createDate 2023-11-09 16:32:34
 * @Entity com.luoying.model.entity.Question
 */
public interface AcceptedQuestionMapper extends BaseMapper<AcceptedQuestion> {
    /**
     * 向个人通过题目表新增通过题目的id
     * @param tableName 表名
     * @param questionId 题目id
     */
    int addAcceptedQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

    /**
     * 根据表名 获取该用户所有通过的题目
     *
     * @param tableName 表名
     */
    List<AcceptedQuestion> queryAcceptedQuestionList(@Param("tableName") String tableName);

    /**
     * 是否存在表
     *
     * @param tableName 表名
     */
    int existAcceptedQuestionTable(@Param("tableName") String tableName);

    /**
     * 删除通过题目表
     *
     * @param tableName 表名
     */
    int dropAcceptedQuestionTable(@Param("tableName") String tableName);

    /**
     * 创建通过题目表
     *
     * @param tableName 表名
     */
    int createAcceptedQuestionTable(@Param("tableName") String tableName);

}




