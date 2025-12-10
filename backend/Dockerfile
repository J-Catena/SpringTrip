# Etapa 1: build con Maven y JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos pom y código fuente
COPY pom.xml .
COPY src ./src

# Compilar el proyecto sin tests
RUN mvn clean package -DskipTests

# Etapa 2: imagen ligera solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Render pasa la variable PORT, Spring la usa via server.port=${PORT:8080}
ENV PORT=8080
EXPOSE 8080

# Arrancar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
