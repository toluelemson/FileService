# File Service Application

This README provides instructions on how to start the File Service application, which consists of a Spring Boot application connected to a MongoDB database, with Mongo Express as a web-based admin interface.

## Prerequisites

Make sure you have the following installed on your system:

- Docker
- Docker Compose

## Setup Instructions

### Step 1: Clone the Repository

Clone the repository to your local machine:

```sh
https://github.com/toluelemson/FileService.git
cd fileservice
```

### Step 2: Make the Script Executable

Ensure the `start_services.sh` script is executable. Run the following command:

```sh
chmod +x start_services.sh
```

### Step 3: Start the Services

Use the provided script to start the services. This script will build and start the Spring Boot application, MongoDB, and Mongo Express:

```sh
./start_services.sh
```

### Step 4: Access the Services

Once the services are up and running, you can access them via the following URLs:

- **Spring Boot Application**: [http://localhost:8080](http://localhost:8080)
- **Mongo Express**: [http://localhost:8081](http://localhost:8081)

### Step 5: Stopping the Services

To stop the services, press `Ctrl + C` in the terminal where the services are running, or run the following command in another terminal window:

```sh
docker-compose down
```

## Configuration

The Docker Compose file (`docker-compose.yml`) includes the configuration for all services. Below are the key configurations:

### MongoDB

- **Image**: `mongo:latest`
- **Ports**: `27017:27017`
- **Environment Variables**:
  - `MONGO_INITDB_ROOT_USERNAME=admin`
  - `MONGO_INITDB_ROOT_PASSWORD=password`
- **Volume**: `mongodb_data:/data/db`

### Mongo Express

- **Image**: `mongo-express:latest`
- **Ports**: `8081:8081`
- **Environment Variables**:
  - `ME_CONFIG_MONGODB_ADMINUSERNAME=admin`
  - `ME_CONFIG_MONGODB_ADMINPASSWORD=password`
  - `ME_CONFIG_MONGODB_SERVER=mongodb`

### Spring Boot Application

- **Build Context**:
  - `context`: `.` (current directory)
  - `dockerfile`: `Dockerfile`
- **Ports**: `8080:8080`
- **Environment Variables**:
  - `SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/filedb?authSource=admin`

## Troubleshooting

### Common Issues

- **Port Conflicts**: Ensure ports `8080`, `8081`, and `27017` are not being used by other applications.
- **Permission Issues**: Make sure you have the necessary permissions to run Docker commands.

### Viewing Logs

To view the logs of all services, run:

```sh
docker-compose logs
```

To view logs of a specific service, use:

```sh
docker-compose logs <service-name>
```

Replace `<service-name>` with `mongodb`, `mongo-express`, or `springboot-app`.
