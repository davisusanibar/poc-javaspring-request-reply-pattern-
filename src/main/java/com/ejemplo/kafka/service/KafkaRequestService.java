/* (C)2024 */
package com.ejemplo.kafka.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ejemplo.kafka.redis.RedisMessageProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

@Service
public class KafkaRequestService {

    @Autowired private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Autowired
    private RedisMessageProcessor redisMessageProcessor;

    private static final long REQUEST_TIMEOUT = 30000; // 30 segundos de espera maximo!

    @KafkaListener(topics = "topicopedidos.reply", groupId = "bff-spring-group")
    public void listenToResponseTopic(String message) {
        JSONObject response = new JSONObject(message);
        String correlationId = response.getString("correlationId");
        String result = response.getString("result");
        redisMessageProcessor.resolvePendingRequest(correlationId, result);
    }

    public String sendAndReceive(String message) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>("topicopedidos", message);

        // Es opcional: establecer el tópico de respuesta en el header
        record.headers().add(KafkaHeaders.REPLY_TOPIC, "topicopedidos.reply".getBytes());


        // CompletableFuture permite la ejecución de tareas de manera asíncrona y no bloqueante (es
        // declarativo)
        RequestReplyFuture<String, String, String> replyFuture =
                replyingKafkaTemplate.sendAndReceive(record);

        // OPCION 0001: Implementacion Request Reply por defecto
        /*
        // Obtener la respuesta asincrona
        ConsumerRecord<String, String> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
        */

        // OPCION 0002: Implementacion Request Reply con Redis para dar soporte a sistemas distribuidos
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        redisMessageProcessor.addPendingRequest(correlationId, responseFuture);

        return responseFuture
                .orTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .thenApply(result -> {
                    System.out.println("Received response for correlationId: " + correlationId);
                    redisMessageProcessor.storeProcessedMessage(correlationId, result);
                    return String.format("{\"correlationId\":\"%s\",\"result\":\"%s\"}", correlationId, result);
                })
                .exceptionally(ex -> {
                    redisMessageProcessor.rejectPendingRequest(correlationId, ex);
                    return String.format("{\"error\":\"%s\"}", ex.getMessage());
                });

    }
}
