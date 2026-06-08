package com.OdontoHelpBackend.util;

import com.OdontoHelpBackend.domain.usuario.Usuario;
import com.OdontoHelpBackend.infra.util.EmailNormalizer;
import com.OdontoHelpBackend.repository.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioLookupHelper {

    private final UsuarioRepository usuarioRepository;
    private final CryptoService cryptoService;

    public Optional<Usuario> findByEmail(String email) {
        String normalized = EmailNormalizer.normalize(email);
        if (normalized == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailHash(cryptoService.lookupHash(normalized));
    }

    public boolean existsByEmail(String email) {
        String normalized = EmailNormalizer.normalize(email);
        if (normalized == null) {
            return false;
        }
        return usuarioRepository.existsByEmailHash(cryptoService.lookupHash(normalized));
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        String normalized = EmailNormalizer.normalize(email);
        if (normalized == null) {
            return false;
        }
        return usuarioRepository.existsByEmailHashAndIdNot(cryptoService.lookupHash(normalized), id);
    }

    public boolean existsByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }
        return usuarioRepository.existsByCpfHash(cryptoService.lookupHash(cpf.replaceAll("\\D", "")));
    }
}
