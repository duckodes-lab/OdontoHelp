#!/usr/bin/env bash
# Lembrete para restringir SSH na Oracle Cloud apos o deploy.
# Execute no PC: bash scripts/vps/oracle-restrict-ssh-reminder.sh

set -euo pipefail

echo "=== Restringir SSH na Oracle Cloud ==="
echo ""
echo "1. Descubra seu IP publico (IPv4 ou IPv6):"
if command -v curl &>/dev/null; then
  echo -n "   IPv4: "
  curl -4 -s --max-time 8 https://ifconfig.me/ip || echo "(indisponivel)"
  echo -n "   IPv6: "
  curl -6 -s --max-time 8 https://ifconfig.me/ip || echo "(indisponivel)"
else
  echo "   Abra https://ifconfig.me no navegador"
fi

echo ""
echo "2. Oracle Cloud Console:"
echo "   Networking -> Virtual cloud networks -> sua VCN -> Security Lists"
echo "   Edite a regra SSH (porta 22):"
echo "   - Troque Source CIDR de 0.0.0.0/0 para SEU_IP/32 (IPv4)"
echo "     ou SEU_IPV6/128 (IPv6)"
echo ""
echo "3. Se a VM usa NSG, repita a regra no NSG associado."
echo ""
echo "4. Teste em OUTRO terminal antes de fechar a sessao atual:"
echo "   ssh -i SUA_CHAVE ubuntu@163.176.233.243"
echo ""
echo "5. Opcional na VPS: sudo bash scripts/vps/harden-ssh.sh"
