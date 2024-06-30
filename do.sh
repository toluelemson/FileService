#!/bin/bash

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

if ! command_exists docker; then
    echo "Docker is not installed. Please install Docker and try again."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "Pulling the latest images..."
docker pull maven:3.8.5-openjdk-17
docker pull openjdk:17-jdk-slim

echo "Building and starting the services..."
sudo docker-compose up --build

if [ $? -eq 0 ]; then
    echo "Services started successfully."
    echo "Spring Boot Application: http://localhost:8080"
    echo "Mongo Express: http://localhost:8081"
else
    echo "Failed to start the services. Please check the Docker Compose logs for more details."
    exit 1
fi


echo "Displaying logs (press Ctrl+C to exit)..."
docker-compose logs -f
