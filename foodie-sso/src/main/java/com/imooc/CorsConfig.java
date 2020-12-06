package com.imooc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @description: 跨域配置类
 * @author: Young
 * @create: 2020-11-10 08:52
 **/
@Configuration
public class CorsConfig {
    public CorsConfig() {

    }

    @Bean
    public CorsFilter corsFilter(){
        //添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://47.104.85.220");
        config.addAllowedOrigin("http://47.104.85.220:8080");
        config.addAllowedOrigin("http://www.mtv.com:8080");
        config.addAllowedOrigin("http://www.mtv.com");
        config.addAllowedOrigin("http://www.music.com:8080");
        config.addAllowedOrigin("http://www.music.com");


        //设置是否发送cookie信息
        config.setAllowCredentials(true);

        //设置允许请求的方式
        config.addAllowedMethod("*");

        //设置允许的header
        config.addAllowedHeader("*");

        //为url添加映射路径
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**",config);

        return new CorsFilter(corsSource);
    }
}
