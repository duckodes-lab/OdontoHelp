package com.OdontoHelpBackend.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtSecretStartupValidator implements ApplicationRunner {

    private final JwtService jwtService;

    @Override
    public void run(ApplicationArguments args) {
        jwtService.validarChaveSecreta();
    }
}
