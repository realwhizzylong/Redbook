package com.example.service;

import com.example.thrd.bo.UpdateUsersBO;
import com.example.thrd.pojo.Users;

public interface UserService {

    /**
     * 判断用户是否存在，如果存在则返回用户信息
     */
    public Users queryMobileExist(String mobile);

    /**
     * 创建用户信息，并且返回用户对象
     */
    public Users createUser(String mobile);

    /**
     * 根据用户主键查询用户信息
     */
    public Users getUser(String userId);

    /**
     * 修改用户基本信息
     */
    public Users updateUserInfo(UpdateUsersBO updateUsersBO);

    /**
     * 修改用户基本信息
     */
    public Users updateUserInfo(UpdateUsersBO updateUsersBO, int type);

}
