# Hardening da VPS Oracle — pós-incidente SSH

Runbook para resposta a incidente após acesso SSH confirmado em pentest.
Execute na ordem abaixo.

## Pré-requisitos

- Acesso SSH à VPS (antes de restringir a porta 22, use o console Oracle se necessário)
- Projeto em `~/OdontoHelp` (ou ajuste `PROJECT_DIR` nos scripts)
- Arquivo `.env` de produção **somente no servidor** (nunca commitar)

---

## Fase 1 — Firewall Oracle (console web)

**Faça isso PRIMEIRO**, antes de endurecer SSH.

1. Acesse [Oracle Cloud Console](https://cloud.oracle.com)
2. **Networking → Virtual Cloud Networks → sua VCN**
3. Abra a **Security List** da subnet da instância
4. Em **Ingress Rules**, revise:

| Porta | Source | Ação |
|-------|--------|------|
| 22 | `0.0.0.0/0` | **Remover** ou trocar pelo **seu IP fixo**/32 |
| 80 | `0.0.0.0/0` | Manter (Caddy HTTP) |
| 443 | `0.0.0.0/0` | Manter (Caddy HTTPS) |
| 8080, 5432, 5173 | qualquer | **Não deve existir** |

5. Se a instância usa **Network Security Group (NSG)**, repita as mesmas regras no NSG associado à NIC.

6. Na VPS, confirme portas locais:

```bash
bash scripts/vps/check-ports.sh
```

---

## Fase 2 — Auditoria SSH

Na VPS:

```bash
cd ~/OdontoHelp
bash scripts/vps/audit-ssh.sh
```

Salve a saída. Anote IP, data/hora e usuário do pentest para o relatório.

---

## Fase 3 — Rotação de secrets + rebuild

### Gerar novos valores (no seu PC — PowerShell)

```powershell
# JWT_SECRET
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Senha forte (24 chars)
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 24 | ForEach-Object {[char]$_})
```

Gere **três** valores: `POSTGRES_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_PASSWORD`.

### Na VPS

1. Edite `.env` com os novos valores
2. Execute:

```bash
cd ~/OdontoHelp
bash scripts/vps/rotate-secrets-and-rebuild.sh
```

O script:
- Altera senha do Postgres no container (preserva dados)
- Sobe o stack com `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build`
- Mostra logs do backend

### Contas da aplicação

- Troque/desative o usuário de teste passado ao pentester
- Todos devem fazer logout e login novamente (JWT antigo invalidado)

---

## Fase 4 — Endurecer SSH

```bash
cd ~/OdontoHelp
bash scripts/vps/harden-ssh.sh
```

**Antes** de desabilitar senha SSH:

1. No seu PC: `ssh-keygen -t ed25519 -C "vitor-odontohelp"`
2. Copie a chave:

```powershell
type $env:USERPROFILE\.ssh\id_ed25519.pub | ssh usuario@IP_DA_VPS "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
```

3. Teste login por chave em **outro terminal**
4. Só então confirme `PasswordAuthentication no` no script

---

## Fase 5 — Validar exposição externa

Do seu PC ou de máquina externa:

```bash
bash scripts/vps/validate-exposure.sh IP_DA_VPS api-odontohelp.duckdns.org odontohelp.duckdns.org
```

Teste token antigo (deve retornar 401 após rotação do JWT_SECRET):

```bash
curl -i https://api-odontohelp.duckdns.org/agendamentos \
  -H "Authorization: Bearer TOKEN_ANTIGO_DO_PENTEST"
```

---

## Fase 6 — Deploy endurecimento da API

Após `git pull` na VPS com as mudanças de rate limit/lockout:

```bash
cd ~/OdontoHelp
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Valide com [security-testing.md](security-testing.md).

---

## Fase 7 — Relatório do pentester

Use o template [pentest-report-template.md](pentest-report-template.md) e peça ao amigo que preencha.

---

## Checklist final

- [ ] Porta 22 fechada ou restrita ao seu IP (Oracle Security List)
- [ ] `JWT_SECRET`, `POSTGRES_PASSWORD`, `APP_ADMIN_PASSWORD` rotacionados
- [ ] Usuário de teste do pentest desativado ou com senha nova
- [ ] SSH com chave; senha desabilitada
- [ ] `nmap` confirma só 80/443 públicos
- [ ] Token antigo retorna 401
- [ ] Backend sobe sem erro após rebuild
- [ ] Rate limit deployado na VPS
