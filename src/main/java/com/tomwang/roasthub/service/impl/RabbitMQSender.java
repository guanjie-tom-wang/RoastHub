package com.tomwang.roasthub.service.impl;

import com.tomwang.roasthub.dao.pojo.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {
    @Autowired
    private AmqpTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);

    public void send(RegistrationRequest request) {
        logger.info("Sending RabbitMQ: "+request);
        rabbitTemplate.convertAndSend("RoastHubQueue", request);
    }
}
