package com.templar.springboot_testdemo3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry){
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8080") // 允许前端域名
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的 HTTP 方法
                        .allowedHeaders("*") // 允许的请求头
                        .allowCredentials(true); // 允许携带凭证
            }
        };
    }
}
