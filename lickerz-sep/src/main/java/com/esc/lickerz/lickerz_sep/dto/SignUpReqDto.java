package com.esc.lickerz.lickerz_sep.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class SignUpReqDto {
    private String username;
    private String email;
    private String password;
    private Date createdAt;
}
