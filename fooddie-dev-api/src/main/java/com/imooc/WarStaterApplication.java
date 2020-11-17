package com.imooc;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-17 11:07
 **/

public class WarStaterApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //指向Application这个springboot启动类
        return builder.sources(Application.class);
    }
}
