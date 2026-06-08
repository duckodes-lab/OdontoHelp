#!/usr/bin/env bash
# Verifica portas em escuta na VPS. Esperado em prod: 80/443 (Caddy), 22 (SSH local).
set -euo pipefail

echo "=== Portas em escuta (ss -tlnp) ==="
sudo ss -tlnp 2>/dev/null || ss -tlnp

echo ""
echo "=== Containers Docker ==="
docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}" 2>/dev/null || echo "Docker não disponível"

echo ""
echo "=== Verificação rápida ==="
for port in 8080 8081 8082 5432 5173; do
  if ss -tlnp 2>/dev/null | grep -q ":${port} "; then
    echo "ATENÇÃO: porta ${port} está em escuta — não deveria estar pública em produção"
  else
    echo "OK: porta ${port} não exposta localmente"
  fi
done
