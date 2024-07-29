# syntax=docker/dockerfile:1
FROM postgres
ENV POSTGRES_HOST_AUTH_METHOD trust
ENV POSTGRES_PASSWORD 123
ENV POSTGRES_DB server-message
COPY db.sql /docker-entrypoint-initdb.d/ 
