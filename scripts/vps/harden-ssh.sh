#!/usr/bin/env bash
# Endurecimento SSH: fail2ban + ajustes sshd_config.
# IMPORTANTE: configure chave SSH ANTES de desabilitar PasswordAuthentication.
set -euo pipefail

SSHD_CONFIG="/etc/ssh/sshd_config"
BACKUP="/etc/ssh/sshd_config.bak.$(date +%Y%m%d%H%M%S)"
DROP_IN_DIR="/etc/ssh/sshd_config.d"
DROP_IN_FILE="${DROP_IN_DIR}/99-odontohelp-hardening.conf"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Execute com sudo: sudo bash scripts/vps/harden-ssh.sh"
  exit 1
fi

echo "=== 1. Backup sshd_config ==="
cp "$SSHD_CONFIG" "$BACKUP"
echo "Backup: $BACKUP"

echo ""
echo "=== 2. Instalar fail2ban (se ausente) ==="
if command -v apt-get &>/dev/null; then
  apt-get update -qq
  apt-get install -y fail2ban
  systemctl enable --now fail2ban
  fail2ban-client status sshd 2>/dev/null || echo "fail2ban instalado; jail sshd pode precisar de ajuste"
else
  echo "apt-get não disponível — instale fail2ban manualmente"
fi

echo ""
echo "=== 3. Drop-in de hardening SSH ==="
mkdir -p "$DROP_IN_DIR"
cat > "$DROP_IN_FILE" << 'EOF'
# OdontoHelp VPS hardening
PermitRootLogin no
MaxAuthTries 3
PubkeyAuthentication yes
# Descomente a linha abaixo SOMENTE após testar login por chave em outro terminal:
# PasswordAuthentication no
EOF

echo "Criado: $DROP_IN_FILE"
echo ""
echo "ATENÇÃO: PasswordAuthentication ainda está comentado."
echo "Após copiar sua chave SSH e testar login, edite:"
echo "  sudo nano $DROP_IN_FILE"
echo "Descomente: PasswordAuthentication no"

echo ""
echo "=== 4. Validar e reiniciar sshd ==="
sshd -t
systemctl restart sshd
echo "sshd reiniciado."

echo ""
echo "=== Próximos passos ==="
echo "1. Copie sua chave pública para ~/.ssh/authorized_keys"
echo "2. Teste: ssh usuario@IP em OUTRO terminal"
echo "3. Descomente PasswordAuthentication no em $DROP_IN_FILE"
echo "4. sudo systemctl restart sshd"
