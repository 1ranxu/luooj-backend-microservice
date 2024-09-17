package com.luoying.luoojbackendmodel.dto.contest_apply;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 17:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestApplyQueryRequest extends PageRequest implements Serializable {
    /**
     * 竞赛id
     */
    private Long contestId;

    /**
     * 参赛者id
     */
    private Long applicantId;

    private static final long serialVersionUID = 1L;
}
