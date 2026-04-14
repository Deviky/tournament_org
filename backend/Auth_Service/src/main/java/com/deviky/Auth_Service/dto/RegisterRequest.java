package com.deviky.Auth_Service.dto;


import com.deviky.Auth_Service.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    String email;
    String nickname;
    String password;
    Role role;
}
