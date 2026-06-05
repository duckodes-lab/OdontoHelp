#!/usr/bin/env bash
# Auditoria de acessos SSH — executar na VPS após incidente.
set -euo pipefail

OUT_DIR="${1:-./audit-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$OUT_DIR"

echo "Salvando auditoria em: $OUT_DIR"

{
  echo "=== Data da auditoria: $(date -Iseconds) ==="
  echo ""
  echo "=== Últimos logins aceitos (auth.log) ==="
  sudo grep "Accepted" /var/log/auth.log 2>/dev/null | tail -50 || echo "(auth.log não encontrado)"
  echo ""
  echo "=== Histórico last ==="
  last -20 2>/dev/null || echo "(last não disponível)"
  echo ""
  echo "=== Tentativas falhas recentes (lastb) ==="
  sudo lastb 2>/dev/null | head -20 || echo "(lastb não disponível ou sem falhas)"
  echo ""
  echo "=== Usuários com shell ==="
  grep -E '/bin/(bash|sh)$' /etc/passwd 2>/dev/null || true
  echo ""
  echo "=== sshd_config (trechos relevantes) ==="
  sudo grep -E '^(PasswordAuthentication|PubkeyAuthentication|PermitRootLogin|Port) ' /etc/ssh/sshd_config 2>/dev/null || true
} | tee "$OUT_DIR/ssh-audit.txt"

echo ""
echo "Auditoria salva em: $OUT_DIR/ssh-audit.txt"
echo "Revise IPs e horários do pentest e guarde para o relatório."
