package com.luoying.luoojbackendmodel.dto.contest_apply;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 17:10
 */
@Data
public class ContestApplyDeleteRequest implements Serializable {
    /**
     * 竞赛id
     */
    private Long contestId;

    private static final long serialVersionUID = 1L;
}
