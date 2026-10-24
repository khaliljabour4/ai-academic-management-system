# ALL-IN-ONE image: MariaDB + Spring Boot + FastAPI + nginx in a single container.
# For hosts that run one free container and give you no shell. Everything initialises itself on start.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline
COPY backend/src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
        mariadb-server nginx supervisor python3 python3-venv openssl ca-certificates \
    && rm -rf /var/lib/apt/lists/* /etc/nginx/sites-enabled/default

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

# AI service (models are trained while the image is built)
COPY Ai_features/requirements.txt /app/ai/
RUN python3 -m venv /app/venv && /app/venv/bin/pip install --no-cache-dir -r /app/ai/requirements.txt
COPY Ai_features/main.py Ai_features/train_model.py Ai_features/generate_data.py Ai_features/training_data.csv /app/ai/
RUN cd /app/ai && /app/venv/bin/python train_model.py

# Frontend + process config
COPY web_fronted /usr/share/nginx/html
COPY deploy/nginx.conf /etc/nginx/conf.d/app.conf
COPY deploy/supervisord.conf /etc/supervisor/conf.d/app.conf
COPY deploy/start.sh /start.sh
RUN sed -i 's/\r$//' /start.sh /etc/nginx/conf.d/app.conf /etc/supervisor/conf.d/app.conf && chmod +x /start.sh

EXPOSE 80
CMD ["/start.sh"]
