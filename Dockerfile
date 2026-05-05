# Stage 1: Build the Quarkus Application
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

# Copiamos primero los archivos de configuración para aprovechar el caché de Docker
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ ./gradle/
COPY gradlew ./

# Descargamos las dependencias (opcional, ayuda al caché)
RUN ./gradlew dependencies --no-daemon || true

# Copiamos el resto del código fuente
COPY src/ ./src/

# Compilamos la aplicación (omitiendo tests para acelerar el build en CI)
RUN ./gradlew build -Dquarkus.package.type=fast-jar -x test --no-daemon

# Stage 2: Create the runtime image
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.20

ENV LANGUAGE='en_US:en'

# Copiamos las capas generadas por Quarkus en el stage anterior
COPY --chown=185 --from=build /app/build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /app/build/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /app/build/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
