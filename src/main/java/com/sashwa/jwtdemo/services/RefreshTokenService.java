package com.sashwa.jwtdemo.services;

import com.sashwa.jwtdemo.entities.RefreshToken;
import com.sashwa.jwtdemo.entities.User;
import com.sashwa.jwtdemo.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserService userService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtService jwtService,
                               UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public RefreshToken createRefreshToken(String username) {
        User user = userService.getUserByEmail(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(user)
                .token((jwtService.generateToken(user)))
                .expiryDate(Instant.now().plusMillis(600000)) // set expiry of refresh token to 10 minutes - you can configure it application.properties file
                .build();
        return refreshTokenRepository.save(refreshToken);
    }



    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

}
