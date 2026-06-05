#!/usr/bin/env bash
# Validação externa de portas e endpoints. Executar do PC ou máquina externa.
# Uso: bash scripts/vps/validate-exposure.sh IP_VPS [API_DOMAIN] [FRONT_DOMAIN]
set -euo pipefail

IP="${1:-}"
API_DOMAIN="${2:-}"
FRONT_DOMAIN="${3:-}"

if [[ -z "$IP" ]]; then
  echo "Uso: $0 IP_VPS [API_DOMAIN] [FRONT_DOMAIN]"
  echo "Exemplo: $0 123.45.67.89 api-odontohelp.duckdns.org odontohelp.duckdns.org"
  exit 1
fi

echo "=== nmap portas críticas em $IP ==="
if command -v nmap &>/dev/null; then
  nmap -p 22,80,443,8080,5432 --open "$IP" || true
else
  echo "nmap não instalado — testando com bash /dev/tcp"
  for port in 22 80 443 8080 5432; do
    if timeout 2 bash -c "echo >/dev/tcp/$IP/$port" 2>/dev/null; then
      echo "OPEN: $port"
    else
      echo "closed/filtered: $port"
    fi
  done
fi

echo ""
echo "=== Esperado ==="
echo "- 80, 443: open"
echo "- 22: closed/filtered (ou restrito ao seu IP)"
echo "- 8080, 5432: closed/filtered"

if [[ -n "$API_DOMAIN" ]]; then
  echo ""
  echo "=== API: HEAD $API_DOMAIN/auth/login ==="
  curl -sI "https://${API_DOMAIN}/auth/login" | head -5 || curl -sI "http://${API_DOMAIN}/auth/login" | head -5
fi

if [[ -n "$FRONT_DOMAIN" ]]; then
  echo ""
  echo "=== Front: HEAD $FRONT_DOMAIN/login ==="
  curl -sI "https://${FRONT_DOMAIN}/login" | head -5 || curl -sI "http://${FRONT_DOMAIN}/login" | head -5
fi

echo ""
echo "=== Teste token antigo (opcional) ==="
echo 'curl -i https://API_DOMAIN/agendamentos -H "Authorization: Bearer TOKEN_ANTIGO"'
echo "Esperado após rotação JWT_SECRET: HTTP 401"
