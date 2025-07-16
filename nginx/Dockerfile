# nginx 디폴트 이미지 사용
FROM nginx:alpine

# nginx 설정 복사
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/conf.d/ /etc/nginx/conf.d/

# 정적 파일 복사
COPY src/main/resources/static /usr/share/nginx/html
