package com.deviky.Auth_Service.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterOrganizationRequest extends LoginRequest{
    String organizerName;
    String description;
}
