package org.huhu.contract.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件识别服务接口
 *
 * 职责：接收上传文件 → 本地暂存 → 上传 MinIO → 调用 Python 识别 → 清理本地暂存。
 * 这里只定义契约，具体流程见 RecognitionService 实现。
 */
public interface RecognitionServiceIntreface {

    /**
     * 识别上传的文件
     *
     * @param file 前端传入的 MultipartFile（PDF / 图片）
     * @return 包含文件路径（MinIO URL）、识别结果等信息的 Map
     */
    Map<String, Object> recognize(MultipartFile file);
}
