package com.luoying.luoojbackendmodel.dto.accepted_question;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AcceptedQuestionQueryRequest extends PageRequest implements Serializable {
    /**
     * 通过记录id
     */
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}