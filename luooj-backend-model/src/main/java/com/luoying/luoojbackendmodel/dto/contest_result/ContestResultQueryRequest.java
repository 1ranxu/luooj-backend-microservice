package com.luoying.luoojbackendmodel.dto.contest_result;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/22 20:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestResultQueryRequest extends PageRequest implements Serializable {
    /**
     * 竞赛 id
     */
    private Long contestId;

    private static final long serialVersionUID = 1L;
}
