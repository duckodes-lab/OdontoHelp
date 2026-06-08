#!/usr/bin/env bash
# Deploy de producao na VPS Oracle (executar NA VPS apos git pull).
set -euo pipefail

PROJECT_DIR="${PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
cd "$PROJECT_DIR"

COMPOSE="docker compose -f docker-compose.yml -f docker-compose.prod.yml"
ENV_FILE="${PROJECT_DIR}/.env"

echo "=== OdontoHelp — deploy producao ==="
echo "Diretorio: $PROJECT_DIR"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERRO: .env nao encontrado. Copie .env.production.example para .env e preencha."
  exit 1
fi

echo ""
echo "=== Variaveis obrigatorias (presenca no .env) ==="
for key in POSTGRES_PASSWORD JWT_SECRET CRYPTO_SECRET_KEY APP_ADMIN_PASSWORD \
  FRONTEND_DOMAIN API_DOMAIN FINANCEIRO_API_DOMAIN VITE_API_URL VITE_FINANCEIRO_API_URL \
  CORS_ORIGINS FISCAL_EMISSOR_CNPJ FISCAL_EMISSOR_RAZAO_SOCIAL; do
  if grep -qE "^${key}=.+$" "$ENV_FILE"; then
    echo "OK: $key"
  else
    echo "FALTA: $key"
    exit 1
  fi
done

if [[ ! -f "$PROJECT_DIR/certs/fiscal/"*.pfx ]] 2>/dev/null; then
  echo "AVISO: nenhum .pfx em certs/fiscal/ — NFS-e pode falhar ate copiar o certificado."
fi

echo ""
echo "=== git pull ==="
git pull --ff-only

echo ""
echo "=== docker compose up --build ==="
$COMPOSE up -d --build

echo ""
echo "=== containers ==="
$COMPOSE ps

echo ""
echo "=== validacao local de portas ==="
bash "$PROJECT_DIR/scripts/vps/check-ports.sh"

echo ""
echo "Deploy concluido. Rode validate-exposure.sh do seu PC."
