package com.templar.springboot_testdemo3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="ssh")
@Data
public class SSHconfig {
    private String host;
    private int port = 22;
    private String username;
    private String privateKeyPath;
    private String passphrase;
    private int timeout=5000;
}
