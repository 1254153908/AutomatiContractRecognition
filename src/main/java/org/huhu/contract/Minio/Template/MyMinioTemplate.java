package org.huhu.contract.Minio.Template;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.huhu.contract.Minio.Config.MyMinioConfig;
import org.huhu.contract.Minio.Info.ImageInfo;
import org.huhu.contract.Minio.Properties.MyMinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MyMinioTemplate {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MyMinioConfig minioConfig;
    @Autowired
    private MyMinioProperties myMinioProperties;

    String bucketName = "contract";

    public String uploadFile(MultipartFile file) {

        String url = "";
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(file.getOriginalFilename())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(putObjectArgs);
            String endpoint = myMinioProperties.getEndpoint();
            url = endpoint + "/" + bucketName + "/" + file.getOriginalFilename();
        }catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 按本地文件路径上传到 MinIO（用于识别场景的本地暂存文件）
     *
     * @param filePath   本地文件路径（已暂存好的文件）
     * @param objectName MinIO 上的对象名（建议用 UUID 避免同名覆盖）
     * @return 文件可访问 URL，失败返回空串
     */
    public String uploadFile(Path filePath, String objectName) {
        String url = "";
        try (InputStream in = Files.newInputStream(filePath)) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(in, Files.size(filePath), -1)
                    .contentType(Files.probeContentType(filePath))
                    .build();
            minioClient.putObject(putObjectArgs);
            String endpoint = myMinioProperties.getEndpoint();
            url = endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }



}
