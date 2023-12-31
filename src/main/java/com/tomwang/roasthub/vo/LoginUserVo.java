package com.tomwang.roasthub.vo;

import lombok.Data;

@Data
public class LoginUserVo {
    //与页面交互

    private String id;

    private String account;

    private String nickname;

    private String avatar;
}
