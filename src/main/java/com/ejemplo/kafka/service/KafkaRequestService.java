/* (C)2024 */
package com.ejemplo.kafka.service;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

@Service
public class KafkaRequestService {

    @Autowired private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public String sendAndReceive(String message) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>("topicopedidos", message);

        // Es opcional: establecer el tópico de respuesta en el header
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "topicopedidos.reply".getBytes());

        // CompletableFuture permite la ejecución de tareas de manera asíncrona y no bloqueante (es
        // declarativo)
        RequestReplyFuture<String, String, String> replyFuture =
                replyingKafkaTemplate.sendAndReceive(record);

        // Obtener la respuesta asincrona
        ConsumerRecord<String, String> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }
}
