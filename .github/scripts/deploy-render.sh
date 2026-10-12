#!/usr/bin/env bash
# Dispara o Deploy Hook do Render com a imagem do commit e espera a nova versão responder.
#
# Variáveis esperadas:
#   AMBIENTE      staging | production (só para as mensagens)
#   DEPLOY_HOOK   URL do Deploy Hook do serviço no Render (secret)
#   APP_URL       URL pública do serviço (variable)
#   IMAGEM        imagem completa, ex.: ghcr.io/dono/comed-esg:<sha>
#   VERSAO        SHA esperado em /api/info
#   TENTATIVAS    opcional, padrão 48 (48 x 15 s = 12 min, cobre o cold start do plano free)
set -euo pipefail

TENTATIVAS="${TENTATIVAS:-48}"
INTERVALO=15

if [[ -z "${DEPLOY_HOOK:-}" ]]; then
  echo "::error title=Secret ausente::O Deploy Hook do ambiente ${AMBIENTE} não está configurado. Cadastre o secret RENDER_DEPLOY_HOOK_* (veja docs/SETUP.md)."
  exit 1
fi
if [[ -z "${APP_URL:-}" ]]; then
  echo "::error title=Variable ausente::A URL do ambiente ${AMBIENTE} não está configurada. Cadastre a variable STAGING_URL/PROD_URL (veja docs/SETUP.md)."
  exit 1
fi
APP_URL="${APP_URL%/}"

echo "🚀 Disparando deploy de ${IMAGEM} em ${AMBIENTE}"
curl -fsS -G "$DEPLOY_HOOK" --data-urlencode "imgURL=${IMAGEM}" -o /dev/null
echo "Deploy Hook aceito pelo Render."

echo "🔎 Aguardando ${APP_URL}/api/info responder com versao=${VERSAO}"
versao_atual=""
for ((i = 1; i <= TENTATIVAS; i++)); do
  versao_atual="$(curl -fsS --max-time 20 "${APP_URL}/api/info" 2>/dev/null | jq -r '.versao // empty' || true)"
  if [[ "$versao_atual" == "$VERSAO" ]]; then
    echo "✅ Nova versão no ar após ~$(((i - 1) * INTERVALO)) s."
    break
  fi
  echo "  tentativa ${i}/${TENTATIVAS}: versao='${versao_atual:-sem resposta}'"
  if ((i < TENTATIVAS)); then sleep "$INTERVALO"; fi
done

if [[ "$versao_atual" != "$VERSAO" ]]; then
  echo "::error title=Smoke test falhou::${AMBIENTE} não passou a responder com a versão ${VERSAO} em $((TENTATIVAS * INTERVALO)) s."
  exit 1
fi

status="$(curl -fsS --max-time 20 "${APP_URL}/actuator/health" | jq -r '.status')"
if [[ "$status" != "UP" ]]; then
  echo "::error title=Health check falhou::${APP_URL}/actuator/health respondeu status=${status}."
  exit 1
fi
echo "✅ /actuator/health: UP"

{
  echo "## ✅ Deploy em ${AMBIENTE}"
  echo ""
  echo "| | |"
  echo "|---|---|"
  echo "| URL | ${APP_URL} |"
  echo "| Imagem | \`${IMAGEM}\` |"
  echo "| /api/info → versao | \`${versao_atual}\` |"
  echo "| /actuator/health | UP |"
} >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
