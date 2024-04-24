FROM openjdk:17
#ARG JAR_FILE=target/*.jar
#COPY ./target/back_bp-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-jar","/app.jar"]
#WORKDIR /app
#COPY target/app.jar.jar app.jar.jar
#EXPOSE 8080
#
##ADD target/app.jar app.jar
#ENTRYPOINT #ENTRYPOINT ["java", "-jar","/app.jar"]


WORKDIR /app

# Скопируйте JAR-файл собранного приложения в контейнер
COPY target/app.jar app.jar

# Укажите порт, который будет открыт в контейнере
EXPOSE 8080

# Запустите приложение при старте контейнера
CMD ["java", "-jar", "app.jar"]