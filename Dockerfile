# --- ETAPA 1: BUILDER (Compilación, Tests y Empaquetado) ---
# Usamos una imagen que incluye Maven y Java 21 para compilar.
FROM maven:3.9-eclipse-temurin-21 AS builder

# Directorio de trabajo en el contenedor
WORKDIR /app

# 1. Copia solo el pom.xml. Maven lo usa para descargar dependencias y cachearlas.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copia el código fuente completo.
COPY src ./src

# 3. Compila y empaqueta el JAR (esto ejecuta los tests).
# Si los tests fallan, el build falla aquí.
RUN mvn clean package -DfinalName=app -Djar.finalName=app -Dspring-boot.repackage.skip=true

# Renombramos el JAR final al formato simple esperado por el runtime
RUN mv target/app.jar target/app.jar


# --- ETAPA 2: RUNNER (Ejecución - Imagen ligera) ---
# Usamos solo el Java Runtime (JRE) 21 en una imagen ligera (Alpine).
FROM eclipse-temurin:21-jre-alpine

# Establecemos el directorio de trabajo
WORKDIR /app

# Exponemos el puerto por defecto (8080)
EXPOSE 8080

# Copiamos SOLAMENTE el archivo JAR ejecutable de la etapa 'builder'
COPY --from=builder /app/target/app.jar /app/app.jar

# Comando de inicio: ejecuta el JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]