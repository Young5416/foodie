package com.imooc.service;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-09 21:24
 **/

public interface UserService {

    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);
}
