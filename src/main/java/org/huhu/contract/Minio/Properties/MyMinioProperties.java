package org.huhu.contract.Minio.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "myminio")
public class MyMinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
