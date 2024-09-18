/* (C)2024 */
package com.ejemplo.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerService {

    @KafkaListener(
            topics = "topicopedidos",
            groupId = "processing_group",
            containerFactory = "replyKafkaListenerContainerFactory")
    @SendTo // Responde al tópico especificado en KafkaHeaders.REPLY_TOPIC
    public String listen(ConsumerRecord<String, String> record) {
        // Procesar el mensaje
        String response =
                "CorrelationId: "
                        + extractHeaderAsString(record.headers(), KafkaHeaders.CORRELATION_ID)
                        + ", Procesado: "
                        + record.value();
        return response;
    }

    private String extractHeaderAsString(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header != null) {
            return bytesToHex(header.value());
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
