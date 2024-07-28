package com.sashwa.jwtdemo.controllers;

import com.sashwa.jwtdemo.entities.RefreshToken;
import com.sashwa.jwtdemo.entities.User;
import com.sashwa.jwtdemo.model.JwtResponseDTO;
import com.sashwa.jwtdemo.model.LoginUserDto;
import com.sashwa.jwtdemo.model.RegisterUserDto;
import com.sashwa.jwtdemo.response.LoginResponse;
import com.sashwa.jwtdemo.services.AuthenticationService;
import com.sashwa.jwtdemo.services.JwtService;
import com.sashwa.jwtdemo.services.RefreshTokenService;
import com.sashwa.jwtdemo.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    public AuthenticationController(JwtService jwtService,
                                    AuthenticationService authenticationService,
                                    RefreshTokenService refreshTokenService,
                                    UserService userService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        LoginResponse loginResponse;
        Authentication authentication = authenticationService.authentication(loginUserDto);
        if (authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(loginUserDto.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginUserDto.getEmail());
            loginResponse = LoginResponse.builder()
                    .token(jwtService.generateToken(user))
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getExpirationTime())
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody String  token){
        return refreshTokenService.findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo);
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(token).build();
                }).orElseThrow(() ->new RuntimeException("Refresh Token is not in DB..!!"));
    }
}