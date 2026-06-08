package br.com.odontohelp.fiscal.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretStartupValidator implements ApplicationRunner {

    private final JwtService jwtService;

    public JwtSecretStartupValidator(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void run(ApplicationArguments args) {
        jwtService.validarChaveSecreta();
    }
}
