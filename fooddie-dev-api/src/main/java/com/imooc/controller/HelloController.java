package com.imooc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-09 14:14
 **/

@RestController
public class HelloController {
    @GetMapping("hello")
    public String hello(){
        return "hello";
    }
}
