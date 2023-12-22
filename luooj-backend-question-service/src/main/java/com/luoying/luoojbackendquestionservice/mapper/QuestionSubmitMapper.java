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
     * 根据 个人提交表 获取该用户所有通过的题目
     *
     * @param tableName
     * @return
     */
    List<QuestionSubmit> queryQuestionSubmitList(@Param("tableName") String tableName);

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




