package com.tomwang.roasthub.service;

import com.tomwang.roasthub.dao.pojo.Users;

public interface SysUserService {

//    UserVo findUserVoById(Long id);

    Users findUserById(Long id);

    Users findUser(String account, String password);

    /**
     * 根据token查询用户信息
     * @param token
     * @return
     */
//    Result findUserByToken(String token);

    /**
     * 根据账户查找用户
     * @param account
     * @return
     */
    Users findUserByAccount(String account);

    /**
     * 保存用户
     * @param user
     */
    void save(Users user);

    void delete(Users user);
}
