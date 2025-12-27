# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jre-alpine

# Zainstaluj wait-for-it
RUN apk add --no-cache bash curl
RUN curl -o /wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh \
    && chmod +x /wait-for-it.sh

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "db:5432", "--timeout=60", "--", "java", "-jar", "app.jar"]