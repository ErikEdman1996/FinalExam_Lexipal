# Railway Production Launch Plan For Lexipal

## Summary

Deploy the full Lexipal app to one Railway project using generated Railway domains first: a public Vite frontend, a public Spring Boot backend, a private PostgreSQL database for Lexipal data, and Railway-hosted Keycloak with its own PostgreSQL database.

A backend `Dockerfile` is useful for deterministic Java 21 builds, but the bigger work is production config: Railway `PORT`, database env vars, CORS/WebSocket origins, Keycloak URLs, frontend build-time env vars, health checks, and post-deploy Keycloak client settings.

References:

- Railway Spring Boot guide: https://docs.railway.com/guides/spring-boot
- Railway PostgreSQL guide: https://docs.railway.com/guides/postgresql
- Railway variables docs: https://docs.railway.com/variables
- Railway Dockerfile docs: https://docs.railway.com/builds/dockerfiles
- Railway healthcheck docs: https://docs.railway.com/reference/healthchecks
- Railway static hosting guide: https://docs.railway.com/guides/static-hosting
- Railway domains docs: https://docs.railway.com/networking/domains/working-with-domains
- Keycloak hostname docs: https://www.keycloak.org/server/hostname
- Keycloak container docs: https://www.keycloak.org/server/containers

## Current State

- Backend repo: `FinalExam_Lexipal`, Spring Boot 3.4.3, Java 21, Maven, PostgreSQL driver, Flyway migrations.
- Frontend repo: `FinalExam_Lexipal_Frontend`, Vite React app, build output `dist`.
- Frontend already supports `VITE_API_ROOT_URL`, but Keycloak config is hard-coded to `http://localhost:8080`.
- Backend CORS and WebSocket origins are hard-coded to `http://localhost:5173`.
- Backend has no tracked production `application.properties` or `application.yml`.
- Backend has no `Dockerfile`, `railway.toml`, or explicit health endpoint.
- Local Keycloak currently runs in Docker on `localhost:8080`.

## Architecture

Use these Railway services in one Railway project:

- `lexipal-frontend`: public static Vite site.
- `lexipal-backend`: public Spring Boot API and WebSocket service.
- `lexipal-postgres`: private Railway PostgreSQL for app data.
- `lexipal-keycloak`: public Keycloak service.
- `keycloak-postgres`: private PostgreSQL for Keycloak state.

Keep the app database and Keycloak database separate. It costs a bit more, but avoids schema ownership and backup/recovery headaches.

Initial public URLs will be generated Railway domains:

- Frontend: `https://<frontend>.up.railway.app`
- Backend: `https://<backend>.up.railway.app`
- Keycloak: `https://<keycloak>.up.railway.app`

Custom domains can be added later after the first successful launch.

## Required Backend Code Prep

Add production configuration before deploying:

- Add a tracked `application.yml` or `application-prod.yml`; do not use `application.properties` because this repo ignores it.
- Configure the backend to read Railway's runtime port:
  - `server.port: ${PORT:8585}`
- Configure PostgreSQL from Railway variables:
  - `spring.datasource.url: ${SPRING_DATASOURCE_URL}`
  - `spring.datasource.username: ${SPRING_DATASOURCE_USERNAME}`
  - `spring.datasource.password: ${SPRING_DATASOURCE_PASSWORD}`
- Keep Flyway enabled in production:
  - `spring.flyway.enabled: true`
  - `spring.jpa.hibernate.ddl-auto: validate`
- Configure Keycloak JWT validation:
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri: ${LEXIPAL_JWT_ISSUER_URI}`
  - `jwt.auth.converter.resource-id.name: ${LEXIPAL_JWT_RESOURCE_ID:Lexipal_Frontend}`
  - `jwt.auth.converter.principal-attribute: ${LEXIPAL_JWT_PRINCIPAL_ATTRIBUTE:preferred_username}`
- Replace hard-coded CORS/WebSocket origins with env-driven config:
  - `LEXIPAL_ALLOWED_ORIGINS=https://<frontend>.up.railway.app,http://localhost:5173`
  - Use that value in both `WebConfig` and `WebSocketConfig`.
- Add a health endpoint:
  - Preferred: add `spring-boot-starter-actuator` and expose `/actuator/health`.
  - Set Railway backend healthcheck path to `/actuator/health`.

Add a backend `Dockerfile` at repo root for deterministic Java 21 deployment:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

Railway will inject `PORT`; Spring reads it through `server.port`.

## Required Frontend Code Prep

Make Keycloak config env-driven instead of hard-coded.

Replace `src/features/auth/keycloak.ts` with config based on Vite env vars:

```ts
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'Lexipal_Realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'Lexipal_Frontend',
});

export default keycloak;
```

Frontend Railway variables:

- `VITE_API_ROOT_URL=https://<backend>.up.railway.app`
- `VITE_KEYCLOAK_URL=https://<keycloak>.up.railway.app`
- `VITE_KEYCLOAK_REALM=Lexipal_Realm`
- `VITE_KEYCLOAK_CLIENT_ID=Lexipal_Frontend`

Because Vite embeds `VITE_*` variables at build time, changing them requires redeploying the frontend.

Use Railway static hosting or normal Node build:

- Build command: `npm ci && npm run build`
- Output directory: `dist`
- Node version: ensure Railway uses Node 22+, either by Railway detection or by adding an `.nvmrc` with `22`.

## Keycloak Production Setup

Deploy Keycloak on Railway using the official/community Keycloak template or a Docker image service based on `quay.io/keycloak/keycloak`.

Recommended Keycloak service variables:

- `KC_DB=postgres`
- `KC_DB_URL=jdbc:postgresql://${{keycloak-postgres.PGHOST}}:${{keycloak-postgres.PGPORT}}/${{keycloak-postgres.PGDATABASE}}`
- `KC_DB_USERNAME=${{keycloak-postgres.PGUSER}}`
- `KC_DB_PASSWORD=${{keycloak-postgres.PGPASSWORD}}`
- `KC_HTTP_ENABLED=true`
- `KC_PROXY_HEADERS=xforwarded`
- `KC_HOSTNAME_STRICT=false`
- `KC_HEALTH_ENABLED=true`
- `KC_BOOTSTRAP_ADMIN_USERNAME=<admin username>`
- `KC_BOOTSTRAP_ADMIN_PASSWORD=<sealed strong password>`

After Keycloak has a public Railway domain:

- Create or import realm `Lexipal_Realm`.
- Create public OIDC client `Lexipal_Frontend`.
- Enable standard authorization code flow.
- Set valid redirect URIs:
  - `https://<frontend>.up.railway.app/*`
  - `http://localhost:5173/*` for local development, if desired.
- Set web origins:
  - `https://<frontend>.up.railway.app`
  - `http://localhost:5173` for local development, if desired.
- Confirm the issuer URL works:
  - `https://<keycloak>.up.railway.app/realms/Lexipal_Realm/.well-known/openid-configuration`
- Backend `LEXIPAL_JWT_ISSUER_URI` must exactly match:
  - `https://<keycloak>.up.railway.app/realms/Lexipal_Realm`

## Railway Deployment Steps

1. Commit and push backend production-prep changes to `FinalExam_Lexipal`.
2. Commit and push frontend env-driven Keycloak changes to `FinalExam_Lexipal_Frontend`.
3. Create a new Railway project, for example `lexipal-prod`.
4. Add `lexipal-postgres` from Railway's PostgreSQL template.
5. Add Keycloak using Railway's Keycloak template or Docker image flow, plus `keycloak-postgres`.
6. Generate a public Railway domain for Keycloak.
7. Configure the Keycloak realm/client with the frontend URL once available; use a temporary frontend URL placeholder if needed.
8. Add backend service from GitHub repo `FinalExam_Lexipal`.
9. Set backend variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://${{lexipal-postgres.PGHOST}}:${{lexipal-postgres.PGPORT}}/${{lexipal-postgres.PGDATABASE}}`
   - `SPRING_DATASOURCE_USERNAME=${{lexipal-postgres.PGUSER}}`
   - `SPRING_DATASOURCE_PASSWORD=${{lexipal-postgres.PGPASSWORD}}`
   - `LEXIPAL_JWT_ISSUER_URI=https://<keycloak>.up.railway.app/realms/Lexipal_Realm`
   - `LEXIPAL_JWT_RESOURCE_ID=Lexipal_Frontend`
   - `LEXIPAL_JWT_PRINCIPAL_ATTRIBUTE=preferred_username`
   - `LEXIPAL_ALLOWED_ORIGINS=http://localhost:5173`
10. Deploy backend and generate its public Railway domain.
11. Add frontend service from GitHub repo `FinalExam_Lexipal_Frontend`.
12. Set frontend variables:
   - `VITE_API_ROOT_URL=https://<backend>.up.railway.app`
   - `VITE_KEYCLOAK_URL=https://<keycloak>.up.railway.app`
   - `VITE_KEYCLOAK_REALM=Lexipal_Realm`
   - `VITE_KEYCLOAK_CLIENT_ID=Lexipal_Frontend`
13. Deploy frontend and generate its public Railway domain.
14. Update backend variable:
   - `LEXIPAL_ALLOWED_ORIGINS=https://<frontend>.up.railway.app,http://localhost:5173`
15. Redeploy backend.
16. Update Keycloak client redirect URIs and web origins to include the final frontend domain.
17. Redeploy frontend if any `VITE_*` variable changed after the first frontend build.

## Health Checks And Networking

- Backend Railway healthcheck path: `/actuator/health`.
- Keycloak healthcheck path: `/health/ready` if enabled by the template.
- Frontend static site does not need an app health endpoint; Railway static hosting handles serving.
- Do not expose PostgreSQL with TCP Proxy unless you need external DB access.
- Use Railway private service variables for DB connections, not public TCP proxy values.
- WebSocket URL should work through the existing frontend helper because `VITE_API_ROOT_URL=https://backend...` becomes `wss://backend.../lexipal/ws`.

## Local/Production Parity

Keep local development working:

- Backend default port remains `8585` through `${PORT:8585}`.
- Frontend dev proxy can remain pointed at `http://localhost:8585`.
- Keep `http://localhost:5173` in backend allowed origins for local development.
- Keep `http://localhost:5173/*` in Keycloak valid redirect URIs.
- Use `.env.local` in frontend for local values:
  - `VITE_API_ROOT_URL=http://localhost:8585`
  - `VITE_KEYCLOAK_URL=http://localhost:8080`
  - `VITE_KEYCLOAK_REALM=Lexipal_Realm`
  - `VITE_KEYCLOAK_CLIENT_ID=Lexipal_Frontend`

## Optional Data Migration

Assume fresh production data for first launch.

If you need to migrate local app data later:

- Export local PostgreSQL with `pg_dump`.
- Import into Railway PostgreSQL using Railway's TCP proxy temporarily.
- Disable public TCP proxy again after import.
- Do not migrate local Keycloak dev DB blindly unless you intentionally want all local users/realm state in production. Prefer realm export/import plus fresh production users.

## Verification Checklist

Before Railway deploy:

- Backend: `.\mvnw test`
- Frontend: `npm run lint`, `npm run typecheck`, `npm run build`
- Confirm Docker build works locally if Docker is available.
- Confirm frontend build uses env variables, not hard-coded localhost.

After Railway deploy:

- Open Keycloak well-known URL and verify issuer.
- Open backend `/actuator/health`; expect HTTP 200.
- Open frontend domain.
- Click login; confirm redirect to Railway Keycloak and back to frontend.
- Complete onboarding nickname flow.
- Test authenticated backend calls: profile, library, reading lists.
- Test reading-list genre search.
- Test BookCircle chat WebSocket connection.
- Test logout and login again.
- Check backend deploy logs for Flyway success and JWT issuer errors.
- Check browser console for CORS, WebSocket, or mixed-content errors.

## Assumptions

- First production launch uses Railway-generated domains, not custom DNS.
- The full app is deployed on Railway, including Keycloak.
- Production starts with a fresh app database unless you explicitly migrate data later.
- Backend uses Flyway migrations as the source of truth for production schema.
- Keycloak realm is `Lexipal_Realm` and frontend client is `Lexipal_Frontend`.
