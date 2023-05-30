#Сборка образов
docker build -t demo_sm_api_image .
docker build -f PostgresDockerfile -t social_media_db .
#Запуск контейнеров
docker compose up