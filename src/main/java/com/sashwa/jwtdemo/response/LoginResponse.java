package com.sashwa.jwtdemo.response;

import com.sashwa.jwtdemo.entities.RefreshToken;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString(doNotUseGetters = true)
public class LoginResponse {
    private String token;

    private RefreshToken refreshToken;

    private long expiresIn;

}