# Testes de segurança — rate limit, lockout e idempotência

Execute contra a API local (`http://localhost:8080`) ou produção. Substitua variáveis conforme o ambiente.

Para hardening da VPS (SSH, firewall Oracle, rotação de secrets), veja [vps-oracle-hardening.md](vps-oracle-hardening.md).

## 1. Rate limit no login (429)

```bash
for i in {1..10}; do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@odonto.com","senha":"errada"}'
done
```

Esperado: primeiras respostas `401`, depois `429` com header `Retry-After`.

## 2. Lockout de conta (423)

Após 5 tentativas com senha errada para o mesmo usuário:

```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@odonto.com","senha":"errada"}'
```

Esperado: `423 Locked` enquanto `locked_until` estiver no futuro.

## 3. Idempotência em agendamento

```bash
KEY=$(uuidgen 2>/dev/null || powershell -Command "[guid]::NewGuid().ToString()")
TOKEN="SEU_ACCESS_TOKEN"

curl -i -X POST http://localhost:8080/agendamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $KEY" \
  -d '{"pacienteId":1,"dentistaId":1,"dataInicio":"2026-06-10T10:00:00","dataFim":"2026-06-10T10:30:00"}'

curl -i -X POST http://localhost:8080/agendamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $KEY" \
  -d '{"pacienteId":1,"dentistaId":1,"dataInicio":"2026-06-10T10:00:00","dataFim":"2026-06-10T10:30:00"}'
```

Esperado: mesma resposta (status e corpo) nas duas chamadas; apenas um registro no banco.

## 4. Conflito de horário (409)

Duas requisições concorrentes com horários sobrepostos e chaves de idempotência diferentes devem resultar em um `201` e um `409`.

## 5. Forgot password por e-mail

```bash
for i in {1..5}; do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@odonto.com"}'
done
```

Esperado: `204` nas primeiras; depois `429` por IP ou limite por e-mail no serviço.

## 6. Swagger em produção

Com `SPRING_PROFILES_ACTIVE=prod`, `/swagger-ui.html` deve retornar `401` ou `403` (não mais público).
