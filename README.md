# PoC Spring Framework ReplyingKafkaTemplate

## Alcance PoC

PoC para mostrar como [Spring Replying Temokate](https://docs.spring.io/spring-kafka/reference/kafka/sending-messages.html#replying-template) framework implementa de manera nativa el patron
`Request-Repy` out of the box.

## Setup

### Iniciar servicios

```shell
$ docker compose up -d
$ mvn spring-boot:run -Dspring-boot.run.profiles=instance1
$ mvn spring-boot:run -Dspring-boot.run.profiles=instance2
```

### Pruebas

```shell
$ curl -X POST "http://localhost:38080/api/send?message=HolaDesdeInstancia1"
$ curl -X POST "http://localhost:38081/api/send?message=HolaDesdeInstancia2"

```

## Revisar

```shell
@7d409dc4824b:/$ /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topicopedidos --bootstrap-server kafka:9092 --partitions 10 --replication-factor 1;
Created topic topicopedidos.
@7d409dc4824b:/$ /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topicopedidos.reply --bootstrap-server kafka:9092 --partitions 10 --replication-factor 1;
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Created topic topicopedidos.reply.

```