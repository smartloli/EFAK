# kafka-eagle-docker
## Install kafka-eagle with Docker

[Dockerfile](https://github.com/Dr-kyle/kafka-eagle-docker)

### Pulling the image

```sh
docker pull xianlab/kafka-eagle:v1.4.1
```

### Start

```sh
sudo docker run --name kafka-eagle --net host \
-v /conf/system-config.properties:/app/kafka-eagle/conf/system-config.properties \
xianlab/kafka-eagle:v1.4.1
```

### Using custom Docker images

```sh
FROM xianlab/kafka-eagle:v1.4.1
COPY log4j.properties /app/kafka-eagle/conf
COPY system-config.properties /app/kafka-eagle/conf
```
