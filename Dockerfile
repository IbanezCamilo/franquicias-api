# ---------------------------------------------------------------------------
# Etapa 1: construccion
#
# Las dependencias se resuelven antes de copiar el codigo para que Docker cachee
# esa capa: mientras el pom no cambie, un cambio en src no vuelve a descargar
# medio Maven Central.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src/ src/
# Los tests ya se ejecutan en CI; repetirlos aqui exigiria Docker dentro de Docker
# por culpa de Testcontainers.
RUN mvn -B -ntp clean package -DskipTests

# ---------------------------------------------------------------------------
# Etapa 2: ejecucion
#
# Imagen JRE sobre Alpine: la de build pesa mas de 600 MB y aqui no hace falta
# ni el compilador ni Maven.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# curl se instala solo para el HEALTHCHECK.
RUN apk add --no-cache curl \
    && addgroup -S franquicias \
    && adduser -S franquicias -G franquicias

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

# Nunca root: si el proceso se ve comprometido, no es administrador del contenedor.
USER franquicias

EXPOSE 8080

# MaxRAMPercentage hace que la JVM respete el limite del contenedor en lugar de
# calcular el heap sobre la memoria del host. SerialGC rinde mejor que G1 en
# instancias de 512 MB y un solo nucleo, como el plan gratuito de Render.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl --fail --silent http://localhost:${PORT:-8080}/actuator/health || exit 1

# exec para que la JVM sea el PID 1 y reciba las senales de parada del contenedor.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
