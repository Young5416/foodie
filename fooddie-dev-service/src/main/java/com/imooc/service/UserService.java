package com.imooc.service;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBo;

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

    public Users creatUser(UserBo userBo);

}
