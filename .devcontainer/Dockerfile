FROM maven:3.9-eclipse-temurin-17

# Menambahkan git karena devcontainer membutuhkannya untuk mendeteksi repository
RUN apt-get update && apt-get install -y git

WORKDIR /workspace