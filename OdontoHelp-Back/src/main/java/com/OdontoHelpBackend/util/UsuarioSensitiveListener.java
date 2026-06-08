package com.OdontoHelpBackend.util;

import com.OdontoHelpBackend.domain.usuario.Usuario;
import com.OdontoHelpBackend.infra.util.EmailNormalizer;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class UsuarioSensitiveListener {

    @PrePersist
    @PreUpdate
    void sincronizarHashes(Usuario usuario) {
        CryptoService crypto = SpringBeanLocator.getBean(CryptoService.class);
        String email = EmailNormalizer.normalize(usuario.getEmail());
        usuario.setEmailHash(email != null ? crypto.lookupHash(email) : null);
        String cpf = usuario.getCpf();
        if (cpf != null && !cpf.isBlank()) {
            usuario.setCpfHash(crypto.lookupHash(cpf.replaceAll("\\D", "")));
        } else {
            usuario.setCpfHash(null);
        }
    }
}
