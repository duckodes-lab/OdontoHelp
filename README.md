# OdontoHelp

Sistema odontológico: **Spring Boot** + **React/Vite** + **PostgreSQL** + **MinIO** (arquivos).

> Repositório público: **não** commite `.env` com senhas reais. Use `.env.example` (local) ou `.env.production.example` (VPS).  
> A pasta `docs/` é **somente local** (gitignore) — notas, checklists e runbooks não entram no Git.

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/Mac/Linux)
- Para desenvolvimento só do front: Node.js 22+ (opcional)

## Início rápido (Docker — recomendado)

Na raiz do repositório:

```powershell
cd C:\Estudo\OdontoHelp
docker compose up -d --build
```

Opcional: `copy .env.example .env` para customizar portas/senhas.

| Serviço | URL |
|---------|-----|
| Aplicação (front) | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| MinIO (console) | http://localhost:9001 — `minioadmin` / `minioadmin` |

**Login seed (dev):** `admin@odonto.com` / `123456`

### Comandos úteis (dev)

```powershell
docker compose ps
docker compose logs backend --tail 50
docker compose logs frontend --tail 30
docker compose stop frontend          # liberar porta 5173 para Vite
docker compose up -d backend postgres minio
docker compose down
```

Rebuild após mudanças:

```powershell
docker compose build frontend && docker compose up -d frontend
docker compose build backend && docker compose up -d backend
```

## Desenvolvimento do frontend (Vite — hot reload)

Com API e banco no Docker, pare o container do front para não disputar a porta 5173:

```powershell
docker compose stop frontend
cd OdontoHelp-Front\odonto-help-frontend
npm install
npm run dev
```

Abra http://localhost:5173 — o Vite usa `VITE_API_URL` (padrão `http://localhost:8080` no `.env` ou variável de ambiente).

## Desenvolvimento do backend (opcional)

Com Postgres e MinIO no Docker:

```powershell
docker compose up -d postgres minio
cd OdontoHelp-Back
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Requer JDK 22+ e `JAVA_HOME` configurado.

## Produção (VPS)

Na VPS, clone o repositório e crie o `.env` real:

```bash
cp .env.production.example .env
```

Edite com senhas fortes, `VITE_API_URL`, `CORS_ORIGINS`, `APP_FRONTEND_URL` e SMTP.

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Se mudar `VITE_API_URL`, recrie o front (entra no build):

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml build --no-cache frontend
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d frontend
```

Logs na VPS:

```bash
docker ps
docker logs odontohelp-backend --tail 100
docker logs odontohelp-frontend --tail 50
```

## Estrutura

```text
OdontoHelp/
  docker-compose.yml          # desenvolvimento local
  docker-compose.prod.yml     # overlay VPS / produção
  OdontoHelp-Back/              # API Java (Spring Boot)
  OdontoHelp-Front/
    odonto-help-frontend/       # React + Vite + PWA
  docs/                         # local apenas — não versionado
```

## Segurança (repo público)

| Faça | Não faça |
|------|----------|
| `copy .env.example .env` e altere senhas/JWT | Commitar `.env` ou segredos |
| Manter `docs/` só na máquina local | Commitar a pasta `docs/` |
| `docker compose` com variáveis do `.env` | Reutilizar JWT de exemplo em produção |
| Build do front via Docker (`dist/` ignorado) | Subir `dist/` ou `node_modules/` |

Arquivos ignorados: [.gitignore](.gitignore).

### VPS Oracle (produção)

- Na Security List da Oracle: abra **80/443**; restrinja **22** (SSH).
- Após incidente ou pentest: rotacione `POSTGRES_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_PASSWORD` e recrie os containers.
- Valide rate limit e lockout nos endpoints `/auth/*` antes de expor a API publicamente.
