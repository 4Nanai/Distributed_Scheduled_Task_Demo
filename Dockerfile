FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV DB_HOST=mysql-container
ENV DB_PORT=3306
ENV DB_NAME=distributed_task_db
ENV DB_USERNAME=admin
ENV DB_PASSWORD=password

ENV REDIS_HOST=redis-container
ENV REDIS_PORT=6379

ENV ZK_HOST1=zookeeper1-container
ENV ZK_HOST2=zookeeper2-container
ENV ZK_HOST3=zookeeper3-container
ENV ZK_PORT=2181
ENV ELASTIC_JOB_NAMESPACE=elastic-job-lite-springboot

EXPOSE 8080
ENTRYPOINT ["java", \
    "-Dspring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", \
    "-Dspring.datasource.username=${DB_USERNAME}", \
    "-Dspring.datasource.password=${DB_PASSWORD}", \
    "-Dspring.data.redis.host=${REDIS_HOST}", \
    "-Dspring.data.redis.port=${REDIS_PORT}", \
    "-Delasticjob.reg-center.server-lists=${ZK_HOST1}:${ZK_PORT},${ZK_HOST2}:${ZK_PORT},${ZK_HOST3}:${ZK_PORT}", \
    "-Delasticjob.reg-center.namespace=${ELASTIC_JOB_NAMESPACE}", \
    "-jar", "/app.jar"]
