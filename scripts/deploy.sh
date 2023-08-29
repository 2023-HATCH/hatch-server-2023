# 가동중인 awsstudy 도커 중단 및 삭제
#sudo docker ps -a -q --filter "name=awsstudy" | grep -q . && docker stop awsstudy && docker rm awsstudy | true
sudo docker ps -a -q --filter "name=popoback" | grep -q . && docker stop popoback && docker rm popoback | true

# 기존 이미지 삭제
#sudo docker rmi backtony/awsstudy:1.0
sudo docker rmi realginger/popoback:1.0

# 도커허브 이미지 pullq
sudo docker pull realginger/popoback:1.0

# 도커 run
#docker run -d -p 8080:8080 --name awsstudy backtony/awsstudy:1.0
#docker run -d -p 8080:8080 --name popoback realginger/popoback:1.0

# 도커 run (with 환경변수 파일 설정)
#docker run -d --env-file ./.env -p 8080:8080 --name popoback realginger/popoback:1.0

# 도커 run (with 환경변수 파일 설정, 바인드 마운트)
docker run -d --env-file ./.env -v /home/ubuntu/jh/secret/firebase:/jh/secret/firebase -p 8080:8080 --name popoback realginger/popoback:1.0

# 사용하지 않는 불필요한 이미지 삭제 -> 현재 컨테이너가 물고 있는 이미지는 삭제되지 않습니다.
#이거 jenkins 로그보니까 잘 안되고 있는 것 같은데 왜 잘되지? 이런 부분 몇개 있음
docker rmi -f $(docker images -f "dangling=true" -q) || true


## key 파일 복사하기 - 호스트에 있는 파일을 컨테이너 내 특정 경로로 복사 docker cp
## 복사하기 COPY <호스트경로> <컨테이너 내 경로> (절대경로)
#COPY /home/ubuntu/jh/secret/firebase/popo-fcm-key.json popoback:/src/main/resources/secret/firebase/popo-fcm-key.json