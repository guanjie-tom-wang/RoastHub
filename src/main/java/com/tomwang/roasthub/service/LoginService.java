package com.tomwang.roasthub.service;


import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.vo.Result;
import com.tomwang.roasthub.vo.params.LoginParam;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public interface LoginService {
    /**
     * 登陆功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    Users checkToken(String token);

    /**
     * 退出登陆
     * @param token
     * @return
     */
    Result logout(String token);


    /**
     * 注册
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);

    Result delete(Users user);

}
