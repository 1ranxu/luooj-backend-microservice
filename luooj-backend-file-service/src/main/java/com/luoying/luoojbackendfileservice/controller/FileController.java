package com.luoying.luoojbackendfileservice.controller;

import cn.hutool.core.io.FileUtil;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.FileConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendfileservice.manager.CosManager;
import com.luoying.luoojbackendmodel.dto.file.UploadFileRequest;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.FileUploadBizEnum;
import com.luoying.luoojbackendserviceclient.service.UserFeighClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * @author 落樱的悔恨
 * 文件接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class FileController {
    final long ONE_M = 1024 * 1024L;

    @Resource
    private UserFeighClient userFeighClient;

    @Resource
    private CosManager cosManager;

    /**
     * 上传文件
     *
     * @param multipartFile     多部分文件
     * @param uploadFileRequest 上传文件请求
     * @param request           请求
     * @return {@link BaseResponse<String>}
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(UploadFileRequest uploadFileRequest, @RequestPart("file") MultipartFile multipartFile
            , HttpServletRequest request) {
        // 获取文件的业务类型
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {// // 文件不是我们规定的业务类型
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断文件大小和类型是否合规
        validFile(multipartFile, fileUploadBizEnum);
        // 获取登录用户
        User loginUser = this.userFeighClient.getLoginUser(request);
        // 文件目录：业务/用户/文件名
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败，请重试");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param fileUploadBizEnum 业务类型
     * @param multipartFile     多部分文件
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {// 用户头像业务
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
