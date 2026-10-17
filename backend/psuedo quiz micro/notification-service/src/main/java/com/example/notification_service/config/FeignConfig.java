package com.example.notification_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            var attributes = RequestContextHolder.getRequestAttributes();

            if (attributes instanceof ServletRequestAttributes servletRequestAttributes){
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")){
                    requestTemplate.header("Authorization" , token);
                }
            }
        };
    }
}
