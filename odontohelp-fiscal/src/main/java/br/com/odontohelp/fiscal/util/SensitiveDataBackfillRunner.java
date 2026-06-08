package br.com.odontohelp.fiscal.util;

import br.com.odontohelp.fiscal.domain.Nfse;
import br.com.odontohelp.fiscal.repository.NfseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SensitiveDataBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SensitiveDataBackfillRunner.class);

    private final NfseRepository nfseRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int atualizados = 0;
        for (Nfse nfse : nfseRepository.findAll()) {
            if (precisaCifrar(nfse.getTomadorCpfCnpj()) || precisaCifrar(nfse.getTomadorEmail())
                    || precisaCifrar(nfse.getTomadorLogradouro())) {
                nfseRepository.save(nfse);
                atualizados++;
            }
        }
        if (atualizados > 0) {
            log.info("Backfill de criptografia fiscal concluido para {} registro(s)", atualizados);
        }
    }

    private boolean precisaCifrar(String value) {
        return value != null && !value.isBlank() && !cryptoService.isEncrypted(value);
    }
}
