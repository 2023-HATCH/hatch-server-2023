# JDK11 이미지 사용
FROM openjdk:11-jdk

#볼륨 마운트. spring boot의 Tomcat의 default 저장소가 /tmp임. 이렇게 하면 호스트의 /var/lib/docker에 임시파일을 만들고 컨테이너 안의 /tmp 와 연결할 수 있다고 함
VOLUME /tmp

# JAR_FILE 변수에 값을 저장
#ARG JAR_FILE=./build/libs/awsstudy-1.0.jar

#위 경로 아닌 거 같아서 둥지 참고해서 가져옴. 나중에 정확한 파일명과 경로로 바꾸면 좋을 듯
ARG JAR_FILE=build/libs/*.jar

# 변수에 저장된 것을 컨테이너 실행시 이름을 app.jar파일로 변경하여 컨테이너에 저장
COPY ${JAR_FILE} app.jar

## key 파일 복사하기 - 호스트에 있는 파일을 컨테이너 내 특정 경로로 복사
## 복사하기 COPY <호스트경로> <컨테이너 내 경로> (절대경로)
COPY ~/jh/secret/firebase/popo-fcm-key.json /src/main/resources/secret/firebase/popo-fcm-key.json
#ARG FCM_KEY_FILE=

# 빌드된 이미지가 run될 때 실행할 명령어
ENTRYPOINT ["java","-jar","app.jar"]