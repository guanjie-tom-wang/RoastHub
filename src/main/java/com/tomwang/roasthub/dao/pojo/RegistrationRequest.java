package com.tomwang.roasthub.dao.pojo;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String email;
    private String password;
}
