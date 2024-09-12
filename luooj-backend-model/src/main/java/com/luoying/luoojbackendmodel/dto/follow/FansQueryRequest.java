package com.luoying.luoojbackendmodel.dto.follow;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/5 14:12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FansQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
