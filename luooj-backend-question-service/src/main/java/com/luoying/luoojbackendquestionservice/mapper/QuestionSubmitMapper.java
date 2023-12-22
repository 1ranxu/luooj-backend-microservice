package com.luoying.luoojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_submit(题目提交记录)】的数据库操作Mapper
 * @createDate 2023-11-09 16:32:34
 * @Entity com.luoying.model.entity.QuestionSubmit
 */
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {
    /**
     * 向 个人提交表 新增个人的提交记录
     *
     * @param tableName
     * @param questionSubmit
     * @return
     */
    int addQuestionSubmit(@Param("tableName") String tableName, @Param("questionSubmit") QuestionSubmit questionSubmit);

    /**
     * 根据 个人提交表 和题目id 获取用户该题目的提交数量
     * @param tableName
     * @param questionId
     * @return
     */
    long countQuestionSubmitAll(@Param("tableName") String tableName,@Param("questionId") long questionId);

    /**
     * 根据 个人提交表 页面大小 开始号 语言 题目id  获取该用户的提交记录
     * @param tableName
     * @param size
     * @param offset
     * @param language
     * @param questionId
     * @return
     */
    List<QuestionSubmit> queryQuestionSubmitList(@Param("tableName") String tableName, @Param("size") long size, @Param("offset") long offset, @Param("language") String language, @Param("questionId") long questionId);

    /**
     * 根据 个人提交表 和 提交记录id 更新该用户的提交记录
     *
     * @param tableName
     * @return
     */
    int updateQuestionSubmit(@Param("tableName") String tableName, @Param("questionSubmit") QuestionSubmit questionSubmit);

    /**
     *
     */

    /**
     * 是否存在 个人提交表
     *
     * @param tableName
     * @return
     */
    int existQuestionSubmitTable(@Param("tableName") String tableName);

    /**
     * 删除 个人提交表
     *
     * @param tableName
     * @return
     */
    int dropQuestionSubmitTable(@Param("tableName") String tableName);

    /**
     * 创建 个人提交表
     *
     * @param tableName
     * @return
     */
    int createQuestionSubmitTable(@Param("tableName") String tableName);
}




