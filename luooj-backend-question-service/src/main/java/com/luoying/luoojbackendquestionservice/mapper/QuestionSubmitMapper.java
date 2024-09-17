package com.luoying.luoojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.apache.ibatis.annotations.Param;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_submit(题目提交记录)】的数据库操作Mapper
 * @createDate 2023-11-09 16:32:34
 * @Entity com.luoying.model.entity.QuestionSubmit
 */
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {
    /**
     * 分页
     * @param page
     * @param questionSubmitQueryRequest
     * @return
     */
    Page<QuestionSubmit> selectPage(@Param("page") IPage page, @Param("questionSubmitQueryRequest") QuestionSubmitQueryRequest questionSubmitQueryRequest);
}




