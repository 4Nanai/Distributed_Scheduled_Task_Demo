FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=distributed_task_db
ENV DB_USERNAME=admin
ENV DB_PASSWORD=password

ENV REDIS_HOST=redis
ENV REDIS_PORT=6379

ENV ZK_SERVERS=zoo1:2181,zoo2:2181,zoo3:2181
ENV ELASTIC_JOB_NAMESPACE=elastic-job-lite-springboot

EXPOSE 8080
ENTRYPOINT ["java", \
    "-Dspring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", \
    "-Dspring.datasource.username=${DB_USERNAME}", \
    "-Dspring.datasource.password=${DB_PASSWORD}", \
    "-Dspring.data.redis.host=${REDIS_HOST}", \
    "-Dspring.data.redis.port=${REDIS_PORT}", \
    "-Delasticjob.reg-center.server-lists=${ZK_SERVERS}", \
    "-Delasticjob.reg-center.namespace=${ELASTIC_JOB_NAMESPACE}", \
    "-jar", "/app.jar"]
