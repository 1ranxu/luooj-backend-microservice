package com.luoying.luoojbackendcommon.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 落樱的悔恨
 * 删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}