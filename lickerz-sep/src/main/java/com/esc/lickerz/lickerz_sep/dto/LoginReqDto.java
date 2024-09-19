package com.esc.lickerz.lickerz_sep.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReqDto {
    private String username;
    private String password;
    private String role;
}