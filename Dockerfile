# syntax=docker/dockerfile:1

# ============================================================
# Estágio 1 — build: compila o JAR com Maven + JDK 21
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache de dependências: enquanto o pom.xml não mudar, esta camada é reaproveitada
# e o build não baixa tudo de novo a cada alteração no código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam no pipeline (jobs build-test e integration-test) antes da imagem ser gerada.
RUN mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/comed-esg.jar extract --layers --launcher --destination target/extracted

# ============================================================
# Estágio 2 — runtime: só a JRE, sem Maven nem código-fonte
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# O pipeline passa o SHA do commit; /api/info devolve esse valor como "versao".
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

# Usuário sem privilégios; /app/logs recebe o volume de logs no docker-compose.
RUN addgroup -S comed && adduser -S -G comed comed \
 && mkdir -p /app/logs && chown -R comed:comed /app
WORKDIR /app

# Camadas do Spring Boot, da que menos muda para a que mais muda.
COPY --from=build --chown=comed:comed /build/target/extracted/dependencies/ ./
COPY --from=build --chown=comed:comed /build/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=comed:comed /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=comed:comed /build/target/extracted/application/ ./

USER comed
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

# sh -c para expandir JAVA_OPTS; exec para o Java ser o PID 1 e receber o SIGTERM (shutdown gracioso).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
