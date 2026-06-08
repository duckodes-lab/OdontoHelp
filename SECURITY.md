# Seguranca — OdontoHelp Platform

## Servicos

| Servico | Porta | Auth |
|---------|-------|------|
| Core | 8080 | Emissor JWT |
| Financeiro | 8081 | Valida JWT do Core |
| Fiscal | 8082 | Valida JWT + tenant (interno em prod) |

## Variaveis obrigatorias (producao)

- `JWT_SECRET` — minimo 256 bits
- `CRYPTO_SECRET_KEY` — Base64 de 32 bytes (mesma chave nos 3 servicos)
- `FISCAL_EMISSOR_CNPJ` — tenant fiscal incluido no JWT (`tenantId`)
- `CORE_BLACKLIST_URL` — `http://backend:8080/internal/auth/token/blacklist/check`
- `CORS_ORIGINS` — origem do frontend

## Criptografia (LGPD Art. 46)

AES-256-GCM em CPF, e-mail, telefone, endereco (Core), clientes financeiros (Financeiro) e tomador NFS-e (Fiscal). Busca por e-mail/CPF via `email_hash` / `cpf_hash` (HMAC-SHA256).

Backfill automatico no startup cifra registros legados em plaintext.

## JWT

Claims: `perfil`, `usuarioId`, `tenantId`, `privacyPolicyVersion`. Refresh tokens armazenados como SHA-256 hash.

Blacklist centralizada no Core. Financeiro e Fiscal **fail-closed** se Core indisponivel.

## LGPD

- `GET /pacientes/{id}/dados-pessoais` — ADMIN ou titular (PACIENTE)
- `DELETE /pacientes/{id}/anonimizar` — ADMIN (registro clinico mantido, identidade removida)

## Rate limiting

Bucket4j in-memory em todos os servicos. Fiscal: limite extra de emissao (`POST /v1/notas`, 10/min por tenant).

## Deploy checklist

- [ ] Secrets unicos e fortes
- [ ] Swagger desabilitado em prod
- [ ] Fiscal sem porta publica (compose prod)
- [ ] Caddy bloqueia `/internal/*`
- [ ] Mesma `CRYPTO_SECRET_KEY` nos 3 servicos
