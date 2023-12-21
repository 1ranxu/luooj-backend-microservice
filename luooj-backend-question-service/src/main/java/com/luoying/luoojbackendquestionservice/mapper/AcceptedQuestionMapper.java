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
     * 向题目通过表新增通过题目的id
     * @param tableName
     * @param questionId
     * @return
     */
    int addAcceptedQuestion(@Param("tableName") String tableName, @Param("questionId") long questionId);

    /**
     * 根据表名 获取该用户所有通过的题目
     *
     * @param tableName
     * @return
     */
    List<AcceptedQuestion> queryAcceptedQuestionList(@Param("tableName") String tableName);

    /**
     * 是否存在表
     *
     * @param tableName
     * @return
     */
    int existAcceptedQuestionTable(@Param("tableName") String tableName);

    /**
     * 删除通过题目表
     *
     * @param tableName
     * @return
     */
    int dropAcceptedQuestionTable(@Param("tableName") String tableName);

    /**
     * 创建通过题目表
     *
     * @param tableName
     * @return
     */
    int createAcceptedQuestionTable(@Param("tableName") String tableName);

}




