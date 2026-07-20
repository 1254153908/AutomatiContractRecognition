package org.huhu.contract.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.huhu.contract.Minio.Template.MyMinioTemplate;
import org.huhu.contract.service.RecognitionServiceIntreface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件识别服务实现
 *
 * 流程：本地暂存 → 上传 MinIO → 调用 Python 识别 → 清理本地暂存。
 * 设计要点：
 *  1. 本地目录（file.upload-dir）只是“中转暂存区”，不是最终存储；
 *     真正持久化的是 MinIO 上的对象，DB 后续应存 MinIO 的 URL / objectKey。
 *  2. 无论识别成功或失败，finally 里都会删掉本地暂存文件，避免磁盘无限堆积。
 *  3. Python 识别走真实 HTTP 调用（POST {python.ocr-url}，见 callPythonOcr），
 *     返回的合同字段做蛇形→驼峰映射后随结果回填前端。
 */
@Service
public class RecognitionService implements RecognitionServiceIntreface {

    private final MyMinioTemplate minioTemplate;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /** 本地暂存目录（绝对路径，已在 application.yml 配好，如 D:/contract-uploads/） */
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    /** Python OCR 服务地址，application.yml 可配 python.ocr-url，未配则留空 */
    @Value("${python.ocr-url:}")
    private String pythonOcrUrl;

    public RecognitionService(MyMinioTemplate minioTemplate,
                              RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper) {
        this.minioTemplate = minioTemplate;
        this.objectMapper = objectMapper;
        // Python 走大模型识别可能较慢，给足读取超时（120s）
        this.restClient = restClientBuilder
                .requestFactory(ocrRequestFactory())
                .build();
    }

    private static SimpleClientHttpRequestFactory ocrRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(120000);
        return factory;
    }

    @Override
    public Map<String, Object> recognize(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.lastIndexOf(".") > 0) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 1. 校验格式
        String lowerExt = ext.toLowerCase();
        if (!lowerExt.matches("\\.(pdf|jpg|jpeg|png|bmp|gif|webp)$")) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 PDF、JPG、PNG、JPEG、BMP、GIF、WEBP");
        }

        // 2. 本地暂存（中转用，最终会被删除）
        Path dir = Paths.get(uploadDir);
        Path targetPath = null;
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            // 用 UUID 做文件名，避免同名覆盖 / 中文乱码
            String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
            targetPath = dir.resolve(savedName);
            // transferTo 传绝对路径时，直接按该路径写，不会再套 multipart 临时目录
            file.transferTo(targetPath.toFile());

            // 3. 上传到 MinIO，拿到可访问 URL（DB 后续存这个，而非本地路径）
            String minioUrl = minioTemplate.uploadFile(targetPath, savedName);

            // 4. 调用 Python 识别（当前为伪代码/注释，待对接）
            Map<String, Object> ocrResult = callPythonOcr(minioUrl, originalName);

            // 5. 组装返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", originalName);   // 原始文件名
            data.put("savedName", savedName);     // MinIO 上的对象名
            data.put("filePath", minioUrl);       // 持久化地址（MinIO），会回填到表单并随合同入库
            data.put("fileSize", file.getSize());
            // 识别出的合同字段提升到顶层，供前端 AssetForm 的 watch 自动回填（字段名与 ContractBo 对齐）
            if (ocrResult != null) {
                data.putAll(ocrResult);
            }
            return data;

        } catch (Exception e) {
            // 把异常往上抛，由 Controller 统一包装成 R.fail 返回
            throw new RuntimeException("文件识别失败：" + e.getMessage(), e);
        } finally {
            // 6. 无论成功失败，清理本地暂存，避免磁盘堆积
            if (targetPath != null) {
                try {
                    Files.deleteIfExists(targetPath);
                } catch (IOException ignored) {
                    // 删除失败不阻塞主流程，仅忽略（可后续加日志）
                }
            }
        }
    }

    /**
     * 调用 Python OCR 识别服务，返回合同结构化字段（字段名与 ContractBo / 前端对齐）。
     *
     * 请求：POST {python.ocr-url}，JSON 体 { "minioUrl": <MinIO 文件地址>, "filename": <原文件名> }
     * 响应：{ "code": 200, "message": "ok", "data": { 蛇形命名合同字段 } }
     *
     * 这里把 Python 返回的蛇形字段映射成前端 watch 使用的驼峰字段：
     *   contractNo / partyA / partyB / signDate / totalAmount / status
     *   items[ productName, quantity, unitPrice, totalPrice, unit, specification ]
     * （注意：Python 的 AI 识别结果不含 projectName，前端“项目名称”需用户手动补填）
     *
     * @param minioUrl     MinIO 上文件的访问地址（Python 端据此下载并识别；桶已设为公开读）
     * @param originalName 原文件名（用于判断 PDF / 图片分支）
     * @return 合同结构化字段（Map）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callPythonOcr(String minioUrl, String originalName) {
        if (pythonOcrUrl == null || pythonOcrUrl.isBlank()) {
            throw new IllegalStateException("未配置 python.ocr-url，无法调用 Python 识别服务");
        }

        // 1. 组装请求体（对齐 Python ContractRecognizeRequest：minioUrl + filename）
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("minioUrl", minioUrl);
        reqBody.put("filename", originalName == null ? "" : originalName);

        // 2. 发送请求并取回原始 JSON
        String respJson;
        try {
            respJson = restClient.post()
                    .uri(pythonOcrUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reqBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("调用 Python 识别服务失败：" + e.getMessage(), e);
        }

        // 3. 解析外层 BaseResponse 并校验
        Map<String, Object> root;
        try {
            root = objectMapper.readValue(respJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Python 识别服务返回非 JSON：" + e.getMessage(), e);
        }
        Object codeObj = root.get("code");
        int code = codeObj instanceof Number ? ((Number) codeObj).intValue() : -1;
        if (code != 200) {
            throw new RuntimeException("Python 识别服务返回错误：code=" + code + "，message=" + root.get("message"));
        }
        Map<String, Object> data = (Map<String, Object>) root.get("data");
        if (data == null) {
            throw new RuntimeException("Python 识别服务返回的 data 为空");
        }

        // 4. 蛇形 -> 驼峰 映射，交给前端自动回填
        Map<String, Object> result = new HashMap<>();
        result.put("contractNo", data.get("contract_no"));
        result.put("partyA", data.get("party_a"));
        result.put("partyB", data.get("party_b"));
        result.put("signDate", data.get("sign_date"));
        result.put("totalAmount", data.get("total_amount"));
        result.put("status", data.get("status"));

        List<Map<String, Object>> srcItems = (List<Map<String, Object>>) data.get("items");
        if (srcItems != null) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, Object> it : srcItems) {
                Map<String, Object> m = new HashMap<>();
                m.put("productName", it.get("item_name"));
                m.put("quantity", it.get("quantity"));
                m.put("unitPrice", it.get("unit_price"));
                m.put("totalPrice", it.get("amount"));
                m.put("unit", it.get("unit"));
                m.put("specification", it.get("spec"));

                // 解析设备入账信息（equipments 数组）
                List<Map<String, Object>> srcEquip = (List<Map<String, Object>>) it.get("equipments");
                if (srcEquip != null && !srcEquip.isEmpty()) {
                    List<Map<String, Object>> equipList = new ArrayList<>();
                    for (Map<String, Object> eq : srcEquip) {
                        Map<String, Object> em = new HashMap<>();
                        em.put("lydwh", eq.get("lydwh"));
                        em.put("lydwm", eq.get("lydwm"));
                        em.put("zcbhqj", eq.get("zcbhqj"));
                        em.put("zcflh", eq.get("zcflh"));
                        em.put("zcmc", eq.get("zcmc"));
                        em.put("ppxh", eq.get("ppxh"));
                        em.put("gg", eq.get("gg"));
                        em.put("sl", eq.get("sl"));
                        em.put("dj", eq.get("dj"));
                        em.put("je", eq.get("je"));
                        em.put("jldw", eq.get("jldw"));
                        em.put("cj", eq.get("cj"));
                        em.put("ggrq", eq.get("ggrq"));
                        equipList.add(em);
                    }
                    m.put("equipments", equipList);
                }
                items.add(m);
            }
            result.put("items", items);
        }
        return result;
    }
}
