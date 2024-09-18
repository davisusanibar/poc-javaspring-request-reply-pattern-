/* (C)2024 */
package com.ejemplo.kafka.controller;

import com.ejemplo.kafka.redis.RedisMessageProcessor;
import com.ejemplo.kafka.service.KafkaRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired private KafkaRequestService kafkaRequestService;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        try {
            return kafkaRequestService.sendAndReceive(message);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar el mensaje: " + e.getMessage();
        }
    }
}
