package com.nyihtuun.bentosystem.userservice.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties({AwsConfigData.class})
public class BeanConfiguration {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public S3Presigner s3Presigner(AwsConfigData awsConfigData) {
        Region awsRegion = Region.of(awsConfigData.region());
        return S3Presigner.builder()
                          .region(awsRegion)
                          .build();
    }
}
