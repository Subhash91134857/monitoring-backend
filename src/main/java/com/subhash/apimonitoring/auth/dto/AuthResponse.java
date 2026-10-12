package com.subhash.apimonitoring.auth.dto;



import com.subhash.apimonitoring.user.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserDTO user;
}