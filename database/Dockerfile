FROM mysql:8.0
# Scripts in this folder run once, on first start with an empty data dir
COPY init.sql /docker-entrypoint-initdb.d/01-init.sql
# Credentials come from env vars at runtime (see docker-compose.yml) - never bake them in
ENV MYSQL_DATABASE=releaseboard
EXPOSE 3306
