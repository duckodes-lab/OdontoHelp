package com.OdontoHelpBackend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoServiceTest {

    private final CryptoService cryptoService = new CryptoService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cryptoService, "secretKeyBase64",
                "T2RvbnRvSGVscERldkxvY2FsQ3J5cHRvS2V5MjAyNCE=");
        cryptoService.init();
    }

    @Test
    void encryptDecryptRoundTrip() {
        String encrypted = cryptoService.encrypt("12345678901");
        assertThat(cryptoService.decrypt(encrypted)).isEqualTo("12345678901");
    }

    @Test
    void lookupHashDeterministico() {
        assertThat(cryptoService.lookupHash("a@b.com"))
                .isEqualTo(cryptoService.lookupHash("a@b.com"));
    }
}
