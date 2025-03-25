# Build Angular
FROM node:23 AS ng-build

WORKDIR /src

# Install angular cli
RUN npm i -g @angular/cli

COPY client/public public
COPY client/src src
# copy all json files
COPY client/*.json .

# Clean Install node_modules then build angular
RUN npm ci
RUN ng build

# Build Spring Boot Java
FROM openjdk:23-jdk AS j-build

WORKDIR /src

COPY server/.mvn .mvn
COPY server/mvnw .
COPY server/mvnw.cmd .
COPY server/pom.xml .
COPY server/src src
# Copy data directory with workout data
COPY server/data /src/data

# Make mvnw executable
RUN chmod +x mvnw

# Copy angular files over to static directory
COPY --from=ng-build /src/dist/client/browser src/main/resources/static
# Run and compile
RUN ./mvnw package -Dmaven.test.skip=true

# Copy the JAR file over to the final container
FROM openjdk:23-jdk-slim

WORKDIR /app
COPY --from=j-build /src/target/final-project-0.0.1-SNAPSHOT.jar app.jar
# Copy data directory to final container - ADD THIS LINE
COPY --from=j-build /src/data /app/data

ENV SERVER_PORT=8080
# Redis
ENV SPRING_DATA_REDIS_HOST=localhost SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_USERNAME="" SPRING_DATA_REDIS_PASSWORD=""
ENV SPRING_DATA_REDIS_DATABASE=0
# mySQL
ENV SPRING_DATASOURCE_URL="" SPRING_DATASOURCE_USERNAME="" SPRING_DATASOURCE_PASSWORD=""
# Spoonacular ApiKey
ENV MY_API_KEY=""
# Spring Security
ENV SPRING_SECURITY_USER_NAME="" SPRING_SECURITY_USER_PASSWORD=""
# Email Configuration
ENV EMAIL_USERNAME="" EMAIL_PASSWORD=""
ENV EMAIL_FROM="" MAIL_SMTP_SSL_TRUST=""
# JWT Configuration
ENV JWT_SECRET=""
# Workout data path
ENV WORKOUT_FILE_PATH="/app/data/workouts.json"
# Google Configuration
ENV GOOGLE_CLIENT_ID="" GOOGLE_CLIENT_SECRET=""
ENV GOOGLE_REDIRECT_URI=""
# Stripe API Key
ENV STRIPE_API_KEY=""
ENV STRIPE_PUBLIC_KEY=""
# Frontend URL (for callback URLs)
ENV APP_FRONTEND_URL=""

EXPOSE ${PORT}

#SHELL ["/bin/sh","-c"]
ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar