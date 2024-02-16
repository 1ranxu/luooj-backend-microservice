package com.luoying.luoojbackendmodel.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 落樱的悔恨
 * 文件上传请求
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务类型
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}