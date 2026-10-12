# Prints de evidência

Salve cada captura nesta pasta **com exatamente o nome indicado**: o README já aponta para eles.
Só valem capturas de execuções reais.

| # | Arquivo | O que capturar | Onde |
|---|---|---|---|
| 1 | `01-pipeline-visao-geral.png` | Execução do pipeline com o grafo dos 5 jobs verdes | GitHub → Actions → execução do CI/CD |
| 2 | `02-job-build-test.png` | Passos do job *Build e testes unitários* expandidos | Clique no job na execução |
| 3 | `03-resumo-testes.png` | Tabela "Testes unitários e de controller" e "Testes de integração" | Rodapé da página da execução (*Summary*) |
| 4 | `04-imagem-ghcr.png` | Package `comed-esg` com as tags `latest` e `<sha>` | Perfil → Packages → comed-esg |
| 5 | `05-aprovacao-producao.png` | Diálogo *Review deployments* com `production` (ou o registro "approved") | Execução do CI/CD, antes/depois de aprovar |
| 6 | `06-render-deploy-staging.png` | Evento "Deploy live" com a imagem `:<sha>` | Render → comed-esg-staging → Events |
| 7 | `07-render-deploy-prod.png` | Evento "Deploy live" com a imagem `:<sha>` | Render → comed-esg-prod → Events |
| 8 | `08-status-staging.png` | Página de status com o selo **STAGING** (âmbar) e o SHA | `STAGING_URL/` |
| 9 | `09-status-producao.png` | Página de status com o selo **PRODUCTION** (verde) e o mesmo SHA | `PROD_URL/` |
| 10 | `10-indicadores-esg-producao.png` | JSON de `/api/esg/indicadores` | `PROD_URL/api/esg/indicadores` |
| 11 | `11-swagger.png` | Swagger UI com os grupos de endpoints | `/swagger-ui.html` (qualquer ambiente) |
| 12 | `12-docker-compose-ps.png` | Saída de `docker compose ps` com `comed-api` e `comed-mongo` *healthy* | Terminal local |
| 13 | `13-atlas-databases.png` | `comed_staging` e `comed_prod` no Data Explorer | Atlas → projeto CoMed ESG → comed-esg → Browse Collections |

Dica: nos prints 8 e 9, deixe a barra de endereço visível para mostrar a URL de cada ambiente.
