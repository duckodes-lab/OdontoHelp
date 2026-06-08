package com.OdontoHelpFinanceiro.util;

import com.OdontoHelpFinanceiro.domain.ClienteFinanceiro;
import com.OdontoHelpFinanceiro.domain.EnvioLembreteCobranca;
import com.OdontoHelpFinanceiro.domain.PreNfse;
import com.OdontoHelpFinanceiro.repository.ClienteFinanceiroRepository;
import com.OdontoHelpFinanceiro.repository.EnvioLembreteCobrancaRepository;
import com.OdontoHelpFinanceiro.repository.PreNfseRepository;
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

    private final ClienteFinanceiroRepository clienteRepo;
    private final PreNfseRepository preNfseRepo;
    private final EnvioLembreteCobrancaRepository envioRepo;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int atualizados = 0;
        for (ClienteFinanceiro c : clienteRepo.findAll()) {
            if (precisaCifrar(c.getCpf()) || precisaCifrar(c.getEmail()) || precisaCifrar(c.getTelefone())) {
                clienteRepo.save(c);
                atualizados++;
            }
        }
        for (PreNfse p : preNfseRepo.findAll()) {
            if (precisaCifrar(p.getDadosTomadorJson())) {
                preNfseRepo.save(p);
                atualizados++;
            }
        }
        for (EnvioLembreteCobranca e : envioRepo.findAll()) {
            if (precisaCifrar(e.getDestino())) {
                envioRepo.save(e);
                atualizados++;
            }
        }
        if (atualizados > 0) {
            log.info("Backfill de criptografia financeiro concluido para {} registro(s)", atualizados);
        }
    }

    private boolean precisaCifrar(String value) {
        return value != null && !value.isBlank() && !cryptoService.isEncrypted(value);
    }
}
