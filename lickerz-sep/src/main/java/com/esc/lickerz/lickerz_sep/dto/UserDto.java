package com.esc.lickerz.lickerz_sep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.util.Date;

@Getter
@Setter
public class UserDto {

    private UUID id;

    @NotBlank(message = "이름 입력은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "이메일 입력은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Size(max = 30, message = "비밀번호는 30자 이하로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(max = 20)
    private String role;

    private Date createdAt;

    public UserDto(UUID id, String username, String email, String role, Date createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

}
