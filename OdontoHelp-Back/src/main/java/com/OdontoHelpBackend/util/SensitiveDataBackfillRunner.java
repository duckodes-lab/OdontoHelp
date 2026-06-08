package com.OdontoHelpBackend.util;

import com.OdontoHelpBackend.domain.usuario.Endereco;
import com.OdontoHelpBackend.domain.usuario.Usuario;
import com.OdontoHelpBackend.repository.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SensitiveDataBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SensitiveDataBackfillRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int atualizados = 0;
        for (Usuario usuario : usuarios) {
            if (precisaCifrar(usuario.getCpf()) || precisaCifrar(usuario.getEmail()) || precisaCifrar(usuario.getTelefone())) {
                usuarioRepository.save(usuario);
                atualizados++;
                continue;
            }
            Endereco endereco = usuario.getEndereco();
            if (endereco != null && enderecoPrecisaCifrar(endereco)) {
                usuarioRepository.save(usuario);
                atualizados++;
            }
        }
        if (atualizados > 0) {
            log.info("Backfill de criptografia concluido para {} usuario(s)", atualizados);
        }
    }

    private boolean precisaCifrar(String value) {
        return value != null && !value.isBlank() && !cryptoService.isEncrypted(value);
    }

    private boolean enderecoPrecisaCifrar(Endereco endereco) {
        return precisaCifrar(endereco.getRua())
                || precisaCifrar(endereco.getNumero())
                || precisaCifrar(endereco.getComplemento())
                || precisaCifrar(endereco.getBairro())
                || precisaCifrar(endereco.getCidade())
                || precisaCifrar(endereco.getUf())
                || precisaCifrar(endereco.getCep());
    }
}
