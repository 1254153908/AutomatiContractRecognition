package org.huhu.contract.Minio.Config;

import io.minio.MinioClient;
import org.huhu.contract.Minio.Properties.MyMinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyMinioConfig {

    @Autowired
    private MyMinioProperties myMinioProperties;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(myMinioProperties.getEndpoint())
                .credentials(myMinioProperties.getAccessKey(), myMinioProperties.getSecretKey())
                .build();
    }

}
