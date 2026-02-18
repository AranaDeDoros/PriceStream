# ---------- Builder ----------
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_3.3.1 AS builder

WORKDIR /app

COPY project/build.properties project/plugins.sbt ./project/
COPY build.sbt ./

RUN sbt update

COPY . .
RUN sbt stage

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/universal/stage/ ./

RUN chmod +x bin/*

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "bin/$(ls bin | head -n 1)"]