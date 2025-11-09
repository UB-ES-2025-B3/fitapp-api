# -------------------------------------
# ETAPA 1: BUILD (Construcción del JAR)
# -------------------------------------
# Usamos una imagen de Maven con Java 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copia solo el pom.xml para optimizar la caché de dependencias.
COPY pom.xml .

# 2. Descarga todas las dependencias (mejor para CI/CD)
RUN mvn dependency:go-offline -B

# 3. Copia el resto del código fuente (src)
COPY src ./src

# 4. Compila el proyecto y genera el JAR, saltándose los tests.
# Usamos -B para modo batch (no interactivo).
# El nombre del JAR se define por el <artifactId> y <version> del pom.xml.
RUN mvn clean package -DskipTests -B


# ------------------------------------------
# ETAPA 2: RUNTIME (Despliegue de la Aplicación)
# ------------------------------------------
# Usamos una imagen muy ligera y segura: eclipse-temurin con JRE 21 y Alpine
FROM eclipse-temurin:21-jre-alpine
# Define el nombre final del JAR basado en tu pom.xml: fitapp_api-0.0.1-SNAPSHOT.jar
ARG FINAL_JAR_NAME=fitapp_api-0.0.1-SNAPSHOT.jar
WORKDIR /app

# Expone el puerto por defecto (8080)
EXPOSE 8080

# Copia el JAR generado de la etapa 'build' a esta nueva imagen
COPY --from=build /app/target/${FINAL_JAR_NAME} app.jar

# Comando para ejecutar la aplicación.
# En esta etapa, el contenedor debe recibir las ENVS (DB_URL, PRIVATE_KEY, etc.).
ENTRYPOINT ["java","-jar","/app/app.jar"]