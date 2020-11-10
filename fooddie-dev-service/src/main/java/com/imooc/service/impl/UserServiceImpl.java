package com.imooc.service.impl;

import com.imooc.enums.Sex;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBo;
import com.imooc.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.MD5Utils;
import java.util.Date;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-09 21:27
 **/

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UsersMapper usersMapper;

    @Autowired
    public Sid sid;

    public static final String DEFAULT_USER_FACE = "http://i2.hdslb.com/bfs/face/74448a0057a6ab2d3ca73831528cf1e7a1d10d8d.jpg";

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Example example = new Example(Users.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        Users users = usersMapper.selectOneByExample(example);
        return users == null ? false : true;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users creatUser(UserBo userBo) {

        String userId = sid.nextShort();
        Users users = new Users();
        users.setId(userId);
        users.setUsername(userBo.getUsername());
        try {
            users.setPassword(MD5Utils.getMD5Str(userBo.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        users.setNickname(userBo.getUsername());
        users.setFace(DEFAULT_USER_FACE);
        users.setBirthday(DateUtil.stringToDate("1990-01-01"));
        users.setSex(Sex.secret.type);
        users.setCreatedTime(new Date());
        users.setUpdatedTime(new Date());

        usersMapper.insert(users);
        return users;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {
        Example example = new Example(Users.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        Users users = usersMapper.selectOneByExample(example);
        return users;
    }
}
