package com.luoying.luoojbackendmodel.dto.contest;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 15:02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestQueryRequest extends PageRequest implements Serializable {
    /**
     * 竞赛id
     */
    private Long id;

    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 竞赛奖励
     */
    private String award;

    /**
     * 重要提示
     */
    private String tips;

    /**
     * 状态（0-待开始、1-进行中、2-已结束）
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
