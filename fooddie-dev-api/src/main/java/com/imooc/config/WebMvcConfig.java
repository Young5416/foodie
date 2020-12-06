package com.imooc.config;

import com.imooc.controller.interceptor.UserTokenInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-15 20:34
 **/

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    //静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/META-INF/resources/")// 映射swagger2
            .addResourceLocations("file:/testspaces/images/"); //映射本地资源
    }

    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTokenInterceptor())
            .addPathPatterns("/hello")
            .addPathPatterns("/shopcart/add")
            .addPathPatterns("/shopcart/del")
            .addPathPatterns("/address/list")
            .addPathPatterns("/address/add")
            .addPathPatterns("/address/update")
            .addPathPatterns("/address/setDefalut")
            .addPathPatterns("/address/delete")
            .addPathPatterns("/orders/*")
            .addPathPatterns("/center/*")
            .addPathPatterns("/userInfo/*")
            .addPathPatterns("/myorders/*")
            .addPathPatterns("/mycomments/*")
            .excludePathPatterns("/myorders/deliver")
            .excludePathPatterns("/orders/notifyMerchantOrderPaid");

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
